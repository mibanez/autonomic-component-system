package cl.niclabs.scada.acs.component.adl.interfaces;

import cl.niclabs.scada.acs.component.ACSUtils;
import cl.niclabs.scada.acs.component.adl.ACSAdlFactory;
import cl.niclabs.scada.acs.component.controllers.analysis.AnalysisController;
import cl.niclabs.scada.acs.component.controllers.execution.ExecutionController;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.planning.PlanningController;
import com.google.gson.Gson;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.components.Component;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.adl.implementations.Controller;
import org.objectweb.fractal.adl.implementations.ControllerContainer;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.proactive.core.component.adl.interfaces.PAInterfaceLoader;
import org.objectweb.proactive.core.component.adl.types.PATypeInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static cl.niclabs.scada.acs.component.controllers.monitoring.metrics.RemoteMonitoringManager.REMOTE_MONITORING_SUFFIX;

public class ACSInterfaceLoader extends PAInterfaceLoader {

    private static final Logger logger = LoggerFactory.getLogger(ACSAdlFactory.class);
    private final Gson gson = new Gson();

    /**
     * Looks for containers of &lt;interface&gt; nodes.
     * 
     * @param node
     * @throws org.objectweb.fractal.adl.ADLException
     */
	@Override
    protected void checkNode(final Object node, boolean functional) throws ADLException {

        //logger.debug("[PAInterfaceLoader] Analyzing node "+ node.toString());
        if (node instanceof InterfaceContainer) {
            checkInterfaceContainer((InterfaceContainer) node, functional);
        }

        // interfaces defined inside a <component> node are F, even if the component maybe NF
        if (node instanceof ComponentContainer) {
            for (final Component comp : ((ComponentContainer) node).getComponents()) {
                checkNode(comp, true);
            }
        }

        // interfaces defined inside a <controller> node are NF (i.e. they belong to the membrane)
        if (node instanceof ControllerContainer) {
            Controller ctrl = ((ControllerContainer) node).getController();
            if (ctrl != null) {
            	if (functional && node instanceof InterfaceContainer) {
            		addNFAcsInterfaces(ctrl, (InterfaceContainer) node);
            		checkAutonomicControllers(ctrl, (InterfaceContainer) node);
            	}
                checkNode(ctrl, false);

            }
        }

    }

	private void checkAutonomicControllers(Controller ctrl, InterfaceContainer fItfContainer) {
		if (ctrl instanceof ComponentContainer) {
			ComponentContainer cc = (ComponentContainer) ctrl;
			for (final Component comp : cc.getComponents()) {
				if (comp.getName().equals("Slave")) {
					if (comp instanceof InterfaceContainer) {
						InterfaceContainer slaveItfContainer = (InterfaceContainer) comp;
						for (Interface fItf : fItfContainer.getInterfaces()) {
							Interface newFItf = gson.fromJson(gson.toJson(fItf), fItf.getClass());
							Map<String, String> attr = newFItf.astGetAttributes();
							attr.put("role", PATypeInterface.CLIENT_ROLE);
							attr.put("contingency", PATypeInterface.OPTIONAL_CONTINGENCY);
							newFItf.astSetAttributes(attr);

							slaveItfContainer.addInterface(newFItf);
						}
					}
				}
			}
		}
	}

	/**
	 * Add the NF interfaces required to turn this component autonomic. This NF are generated in base of
	 * the F interfaces, and requires that the component definition ADL explicitly indicates the controller
	 * description tag (composite or primitive).
	 *
	 * @param ctrl
	 * @param itfContainer
	 * @throws org.objectweb.fractal.adl.ADLException
	 */
    void addNFAcsInterfaces(Controller ctrl, InterfaceContainer itfContainer) throws ADLException {

        logger.trace("adding nf acs interfaces...");
		String hierarchy = ctrl.getDescriptor();
		if (hierarchy == null) {
			throw new ADLException(ACSInterfaceError.MISSING_CONTROLLER_DESCRIPTION);
		}
	
		InterfaceContainer nfItfContainer = (InterfaceContainer) ctrl;
		Interface[] functionalItfs = itfContainer.getInterfaces();

		// I need at least one interface as a reference.
		if (functionalItfs.length > 0) {

			addControllerInterfaces(functionalItfs[0], nfItfContainer);

			/**
			// Add internal server nf autonomic interface on composite components.
			if (hierarchy.equals("composite")) {

				Interface nfItf = gson.fromJson(gson.toJson(functionalItfs[0]), functionalItfs[0].getClass());

				Map<String, String> attr = nfItf.astGetAttributes();
				attr.put("name", MonitoringControllerImpl.INTERNAL_SERVER_MONITORING_ITF);
				attr.put("role", PATypeInterface.INTERNAL_SERVER_ROLE);
				attr.put("cardinality", PATypeInterface.SINGLETON_CARDINALITY);
				attr.put("contingency", PATypeInterface.OPTIONAL_CONTINGENCY);
				attr.put("signature", MonitoringController.class.getName());
				nfItf.astSetAttributes(attr);
	
				nfItfContainer.addInterface(nfItf);
                logger.trace("adding NF internal server interface \"{}\"", MonitoringControllerImpl.INTERNAL_SERVER_MONITORING_ITF);
			}**/
		}

		for (final Interface fItf : functionalItfs) {

    		String role = fItf.astGetAttributes().get("role");

    		// Add internal client nf autonomic interface to monitor the inner bound component
    		if (role.equals(PATypeInterface.SERVER_ROLE) && hierarchy.equals("composite")) {
    			
    			Interface autonomicNFItf = gson.fromJson(gson.toJson(fItf), fItf.getClass());
    			
    			Map<String, String> attr = autonomicNFItf.astGetAttributes();
    			attr.put("name", attr.get("name") + REMOTE_MONITORING_SUFFIX);
    			attr.put("role", PATypeInterface.INTERNAL_CLIENT_ROLE);
    			attr.put("contingency", PATypeInterface.OPTIONAL_CONTINGENCY);
    			attr.put("cardinality", PATypeInterface.SINGLETON_CARDINALITY);
    			attr.put("signature", MonitoringController.class.getName());
        		autonomicNFItf.astSetAttributes(attr);

        		nfItfContainer.addInterface(autonomicNFItf);
                logger.trace("adding NF internal client interface \"{}\"", attr.get("name") + REMOTE_MONITORING_SUFFIX);
    		}
  
    		// Add external client nf autonomic interface to monitor the external bound component
    		else if (role.equals(PATypeInterface.CLIENT_ROLE)) {
 
    			Interface autonomicNFItf = gson.fromJson(gson.toJson(fItf), fItf.getClass());
    			
    			Map<String, String> attr = autonomicNFItf.astGetAttributes();
    			attr.put("name", attr.get("name") + REMOTE_MONITORING_SUFFIX);
    			attr.put("role", PATypeInterface.CLIENT_ROLE);
    			attr.put("contingency", PATypeInterface.OPTIONAL_CONTINGENCY);

    			String cardinality = autonomicNFItf.astGetAttributes().get("cardinality");
  
    			//if (cardinality != null && cardinality.equals(PATypeInterface.MULTICAST_CARDINALITY)) {
    			//	attr.put("signature", MulticastMonitoringController.class.getName());
    			//} else {
    				attr.put("cardinality", PATypeInterface.SINGLETON_CARDINALITY);
    				attr.put("signature", MonitoringController.class.getName());
    			//}
  
    			autonomicNFItf.astSetAttributes(attr);

        		nfItfContainer.addInterface(autonomicNFItf);
                logger.trace("adding NF external client interface \"{}\"", attr.get("name") + REMOTE_MONITORING_SUFFIX);
    		}   		
		}
	}

	private void addControllerInterfaces(Interface reference, InterfaceContainer destination) {
		
		// Generate a copy of this generated-class interface object
		Interface nfItf = gson.fromJson(gson.toJson(reference), reference.getClass());
		
		// Monitor
		Map<String, String> attr = nfItf.astGetAttributes();
		attr.put("name", ACSUtils.MONITORING_CONTROLLER);
		attr.put("role", PATypeInterface.SERVER_ROLE);
		attr.put("cardinality", PATypeInterface.SINGLETON_CARDINALITY);
		attr.put("contingency", PATypeInterface.OPTIONAL_CONTINGENCY);
		attr.put("signature", MonitoringController.class.getName());
		nfItf.astSetAttributes(attr);
		destination.addInterface(nfItf);
		
		// Analyzer
		nfItf = gson.fromJson(gson.toJson(nfItf), nfItf.getClass());
		attr = nfItf.astGetAttributes();
		attr.put("name", ACSUtils.ANALYSIS_CONTROLLER);
		attr.put("signature", AnalysisController.class.getName());
		nfItf.astSetAttributes(attr);
		destination.addInterface(nfItf);
		
		// Planner
		nfItf = gson.fromJson(gson.toJson(nfItf), nfItf.getClass());
		attr = nfItf.astGetAttributes();
		attr.put("name", ACSUtils.PLANNING_CONTROLLER);
		attr.put("signature", PlanningController.class.getName());
		nfItf.astSetAttributes(attr);
		destination.addInterface(nfItf);
		
		// Executor
		nfItf = gson.fromJson(gson.toJson(nfItf), nfItf.getClass());
		attr = nfItf.astGetAttributes();
		attr.put("name", ACSUtils.EXECUTION_CONTROLLER);
		attr.put("signature", ExecutionController.class.getName());
		nfItf.astSetAttributes(attr);
		destination.addInterface(nfItf);
	}
}
