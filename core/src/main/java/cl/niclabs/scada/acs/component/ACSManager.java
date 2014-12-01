package cl.niclabs.scada.acs.component;

import cl.niclabs.scada.acs.component.controllers.analysis.AnalysisController;
import cl.niclabs.scada.acs.component.controllers.execution.ExecutionController;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.MetricStoreFactory;
import cl.niclabs.scada.acs.component.controllers.planning.PlanningController;
import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.PAInterface;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.control.PABindingController;
import org.objectweb.proactive.core.component.control.PAMembraneController;
import org.objectweb.proactive.core.component.exceptions.NoSuchComponentException;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.core.component.type.PAGCMInterfaceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cl.niclabs.scada.acs.component.controllers.monitoring.metrics.RemoteMonitoringManager.INTERNAL_MONITORING_SUFFIX;
import static cl.niclabs.scada.acs.component.controllers.monitoring.metrics.RemoteMonitoringManager.REMOTE_MONITORING_SUFFIX;


public class ACSManager {

    private static final Logger logger = LoggerFactory.getLogger(ACSManager.class);

    public static final String MONITORING_CONTROLLER = "monitoring-controller";
    public static final String ANALYSIS_CONTROLLER  = "analysis-controller";
    public static final String PLANNING_CONTROLLER  = "planning-controller";
    public static final String EXECUTION_CONTROLLER = "execution-controller";

    public static final String CONTROLLER_CONFIG_PATH = "/org/objectweb/proactive/core/component/componentcontroller/config/default-component-controller-config.xml";

    public static MonitoringController getMonitoringController(Component host) throws NoSuchInterfaceException {
        return (MonitoringController) host.getFcInterface(MONITORING_CONTROLLER);
    }

    public static AnalysisController getAnalysisController(Component host) throws NoSuchInterfaceException {
        return (AnalysisController) host.getFcInterface(ANALYSIS_CONTROLLER);
    }

    public static PlanningController getPlanningController(Component host) throws NoSuchInterfaceException {
        return (PlanningController) host.getFcInterface(PLANNING_CONTROLLER);
    }

    public static ExecutionController getExecutionController(Component host) throws NoSuchInterfaceException {
        return (ExecutionController) host.getFcInterface(EXECUTION_CONTROLLER);
    }

    public static void enableRemoteMonitoring(Component component) throws ACSException {
        if (!(component instanceof PAComponent)) {
            throw new ACSException("The component must be a PAComponent instance",
                    new ClassCastException(String.format("Component %s can't be cast to %s",
                            component.getClass().getName(), PAComponent.class.getName())));
        }

        PAComponent pacomponent = (PAComponent) component;

        String hierarchy = pacomponent.getComponentParameters().getHierarchicalType();
        InterfaceType[] interfaceTypes = pacomponent.getComponentParameters().getInterfaceTypes();

        try {
            String name = GCM.getNameController(component).getFcName();
            PABindingController bindingCtrl = Utils.getPABindingController(component);
            PAMembraneController membraneCtrl = Utils.getPAMembraneController(component);
            Component[] parents = Utils.getPASuperController(component).getFcSuperComponents();
            Component parent = parents.length > 0 ? parents[0] : null;

            enableRemoteMonitoring(parent, membraneCtrl, bindingCtrl, interfaceTypes, hierarchy, name);
        } catch (NoSuchInterfaceException e) {
            throw new ACSException("Can't enable monitoring for " + pacomponent + ": " + e.getMessage(), e);
        }
    }

    private static void enableRemoteMonitoring(Component parent, PAMembraneController membraneCtrl,
            BindingController bindingCtrl, InterfaceType[] interfaceTypes, String hierarchy, String name)
            throws ACSException {

        for(InterfaceType interfaceType : interfaceTypes) {

            logger.trace("Enabling remote monitoring on {}.{}...", name, interfaceType.getFcItfName());
            PAGCMInterfaceType itfType = (PAGCMInterfaceType) interfaceType;
            String itfName = itfType.getFcItfName();

            try {
                if (itfType.isFcClientItf()) {
                    logger.trace("... is client");
                    if (itfType.isGCMSingletonItf() || itfType.isGCMGathercastItf()) {
                        logger.trace("... is singleton/gathercast");
                        PAInterface serverItf = (PAInterface) bindingCtrl.lookupFc(itfType.getFcItfName());
                        if (serverItf != null) {
                            logger.trace("..detected external client {}.{}", name, interfaceType.getFcItfName());
                            enableExternalRemote(parent, membraneCtrl, itfName, serverItf);
                        }
                    } else {
                        // MULTICAST NOT SUPPORTED FOR NOW
                    }
                }
                else if (hierarchy.equals(Constants.COMPOSITE) && itfType.isGCMSingletonItf()) {
                    PAInterface serverItf = (PAInterface) bindingCtrl.lookupFc(itfType.getFcItfName());
                    if (serverItf != null) {
                        logger.trace("..detected internal server {}.{}...", name, interfaceType.getFcItfName());
                        enableInternalRemote(membraneCtrl, itfName, serverItf);
                    }
                }
            } catch (NoSuchInterfaceException e) {
                logger.trace("Can't find server for interface {}: {}", itfType.getFcItfName(), e.getMessage());
                throw new ACSException("Can't find server for interface " + itfType.getFcItfName(), e);
            }
        }
    }

    private static void enableExternalRemote(Component parent, PAMembraneController membraneCtrl, String itfName,
            PAInterface serverItf) throws ACSException {
        try {
            if (membraneCtrl.nfLookupFc(itfName + REMOTE_MONITORING_SUFFIX) != null) {
                logger.trace(".... already bound, bye!");
                return; // break loops
            }

            Component serverComponent = serverItf.getFcItfOwner();
            boolean boundToParent = parent.equals(serverComponent);
            logger.trace(".... is bound parent ? {}", boundToParent);

            MonitoringController serverCtrl = boundToParent ? (MonitoringController) serverComponent.getFcInterface(
                    serverItf.getFcItfName() + REMOTE_MONITORING_SUFFIX) : getMonitoringController(serverComponent);

            bindNFInterfaces(membraneCtrl, itfName, serverCtrl);

            if ( !boundToParent ) {
                logger.trace(".... continuing to {}", GCM.getNameController(serverComponent).getFcName());
                enableRemoteMonitoring(serverComponent);
            }
        } catch (NoSuchInterfaceException | NoSuchComponentException e) {
            throw new ACSException("Can't enable external remote monitoring for " + itfName + ": " + e.getMessage(), e);
        }
    }

    private static void enableInternalRemote(PAMembraneController membraneCtrl, String itfName, PAInterface serverItf)
            throws ACSException {
        try {
            if (membraneCtrl.nfLookupFc(itfName + REMOTE_MONITORING_SUFFIX) != null) {
                return; // break loops
            }

            Component serverComponent = serverItf.getFcItfOwner();
            MonitoringController serverCtrl = getMonitoringController(serverComponent);

            bindNFInterfaces(membraneCtrl, itfName, serverCtrl);

            enableRemoteMonitoring(serverComponent);
        }
        catch (NoSuchInterfaceException e) {
            throw new ACSException("Can't reach remote for " + itfName + " resources: " + e.getMessage(), e);
        } catch (NoSuchComponentException e) {
            e.printStackTrace();
        }
    }

    private static void bindNFInterfaces(PAMembraneController membraneCtrl, String itfName,
            MonitoringController serverCtrl) throws ACSException {

        String localCtrlItf = itfName + INTERNAL_MONITORING_SUFFIX;
        String localItf = itfName + REMOTE_MONITORING_SUFFIX;

        try {
            membraneCtrl.stopMembrane();
            membraneCtrl.nfBindFc(MetricStoreFactory.COMPONENT_NAME + "." + localCtrlItf, localItf);
            membraneCtrl.nfBindFc(localItf, serverCtrl);
            membraneCtrl.startMembrane();
        } catch (Exception e) {
            throw new ACSException("Can't bind the NF interfaces for " + localItf + ": " + e.getMessage(), e);
        }
    }
}
