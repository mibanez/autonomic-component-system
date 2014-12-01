package cl.niclabs.scada.acs.component.factory;

import cl.niclabs.scada.acs.component.ACSManager;
import cl.niclabs.scada.acs.component.controllers.analysis.AnalysisController;
import cl.niclabs.scada.acs.component.controllers.analysis.AnalysisControllerFactory;
import cl.niclabs.scada.acs.component.controllers.analysis.RuleEventListener;
import cl.niclabs.scada.acs.component.controllers.execution.ExecutionController;
import cl.niclabs.scada.acs.component.controllers.execution.ExecutionControllerFactory;
import cl.niclabs.scada.acs.component.controllers.execution.ExecutionControllerImpl;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringManagerFactory;
import cl.niclabs.scada.acs.component.controllers.monitoring.events.GCMPAEventListener;
import cl.niclabs.scada.acs.component.controllers.monitoring.events.GCMPAEventListenerFactory;
import cl.niclabs.scada.acs.component.controllers.monitoring.events.RecordEventListener;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.MetricEventListener;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.MetricStore;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.MetricStoreFactory;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.RemoteMonitoringManager;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.RecordStore;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.RecordStoreFactory;
import cl.niclabs.scada.acs.component.controllers.planning.PlanningController;
import cl.niclabs.scada.acs.component.controllers.planning.PlanningControllerFactory;
import org.apache.log4j.Logger;
import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.NameController;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.body.proxy.UniversalBodyProxy;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.control.*;
import org.objectweb.proactive.core.component.exceptions.NoSuchComponentException;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.core.component.representative.PAComponentRepresentative;
import org.objectweb.proactive.core.component.type.PAGCMInterfaceType;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

import java.util.ArrayList;
import java.util.List;


public class BuildHelper {

    private static final Logger logger = ProActiveLogger.getLogger("ACS");

    private static final String CONTROLLER_CONFIG = "/org/objectweb/proactive/core/component/componentcontroller/config/default-component-controller-config.xml";
    private final PAGCMTypeFactory tf;
    private final PAGenericFactory gf;

    public BuildHelper(ACSFactory factory) throws ACSFactoryException {
        this(factory.getTypeFactory(), factory.getGenericFactory());
    }

    public BuildHelper(PAGCMTypeFactory typeFactory, PAGenericFactory genericFactory) throws ACSFactoryException {
        tf = typeFactory;
        gf = genericFactory;
        if (tf == null || gf == null) {
            String msg = "typeFactory and genericFactory for BuildHelper constructor can't be null";
            throw new ACSFactoryException(msg, new NullPointerException());
        }
    }

    /**
     * Normally, the NF interfaces mentioned here should be those that are going to be implemented by NF component,
     * and the rest of the NF interfaces (that are going to be implemented by object controller) should be in a
     * ControllerDesc file. But the PAComponentImpl ignores the NFType if there is a ControllerDesc file specified,
     * so I better put all the NF interfaces here.
     * That means that I need another method to add the object controllers for the (not yet created) controllers.
     * -- cruz
     */
    public List<InterfaceType> getStandardNfInterfaces() throws ACSFactoryException {
        ArrayList<InterfaceType> nfInterfaces = new ArrayList<>();
        try {
            nfInterfaces.add(tf.createGCMItfType(Constants.CONTENT_CONTROLLER, PAContentController.class.getName(), false, false, "singleton"));
            nfInterfaces.add(tf.createGCMItfType(Constants.BINDING_CONTROLLER, PABindingController.class.getName(), false, false, "singleton"));
            nfInterfaces.add(tf.createGCMItfType(Constants.LIFECYCLE_CONTROLLER, PAGCMLifeCycleController.class.getName(), false, false, "singleton"));
            nfInterfaces.add(tf.createGCMItfType(Constants.SUPER_CONTROLLER, PASuperController.class.getName(), false, false, "singleton"));
            nfInterfaces.add(tf.createGCMItfType(Constants.NAME_CONTROLLER, NameController.class.getName(), false, false, "singleton"));
            nfInterfaces.add(tf.createGCMItfType(Constants.MEMBRANE_CONTROLLER, PAMembraneController.class.getName(), false, false, "singleton"));
            nfInterfaces.add(tf.createGCMItfType(Constants.MULTICAST_CONTROLLER, PAMulticastController.class.getName(), false, true, "singleton"));
        } catch (InstantiationException e) {
            throw new ACSFactoryException("Couldn't create standard NF interfaces", e);
        }
        return nfInterfaces;
    }
    /**
     * Adds the controller objects to the NF interfaces that are not part of the M&M Framework.
     * Normally, PAComponentImpl.addMandatoryControllers should have added already the mandatory
     * MEMBRANE, LIFECYCLE and NAME controllers. Interfaces like BINDING and CONTENT, which are not
     * supposed to be in all components, should have been removed from the component NFType in the appropriate cases.
     */
    public void addObjectControllers(Component component) throws ACSFactoryException {

        if (!(component instanceof PAComponent)) {
            throw new ACSFactoryException("the component must be a PAComponent instance");
        }
        String prompt = "addObjectControllers@".concat(((PAComponent) component).getComponentParameters().getName());

        try {
            PAMembraneController membrane = Utils.getPAMembraneController(component);
            try {
                component.getFcInterface(Constants.CONTENT_CONTROLLER);
                membrane.setControllerObject(Constants.CONTENT_CONTROLLER, PAContentControllerImpl.class.getName());
            } catch (NoSuchInterfaceException e) {
                //logger.trace("[{}] NoSuchInterfaceException: {}", prompt, e.getMessage());
            }
            try {
                component.getFcInterface(Constants.BINDING_CONTROLLER);
                membrane.setControllerObject(Constants.BINDING_CONTROLLER, PABindingControllerImpl.class.getName());
                logger.trace("[addObjectControllers] bindingController added");
            } catch (NoSuchInterfaceException e) {
                //logger.trace("[{}] NoSuchInterfaceException: {}", prompt, e.getMessage());
            }
            try {
                component.getFcInterface(Constants.SUPER_CONTROLLER);
                membrane.setControllerObject(Constants.SUPER_CONTROLLER, PASuperControllerImpl.class.getName());
            } catch (NoSuchInterfaceException e) {
                //logger.trace("[{}] NoSuchInterfaceException: {}", prompt, e.getMessage());
            }
            try {
                component.getFcInterface(Constants.MULTICAST_CONTROLLER);
                membrane.setControllerObject(Constants.MULTICAST_CONTROLLER, PAMulticastControllerImpl.class.getName());
            } catch (NoSuchInterfaceException e) {
                //logger.trace("[{}] NoSuchInterfaceException: {}", prompt, e.getMessage());
            }
        } catch (NoSuchInterfaceException e) {
            //logger.trace("[{}] NoSuchInterfaceException: {}", prompt, e.getMessage());
        }
    }


    public List<InterfaceType> getExtraNfInterfaces(InterfaceType[] fInterfaceTypes)
            throws ACSFactoryException {

        String monClazz = MonitoringController.class.getName();
        ArrayList<InterfaceType> extraNfItfs = new ArrayList<>();

        try {
            // Add controller main nf interface
            extraNfItfs.add(tf.createGCMItfType(ACSManager.EXECUTION_CONTROLLER, ExecutionController.class.getName(), false, true, "singleton"));
            extraNfItfs.add(tf.createGCMItfType(ACSManager.PLANNING_CONTROLLER, PlanningController.class.getName(), false, true, "singleton"));
            extraNfItfs.add(tf.createGCMItfType(ACSManager.ANALYSIS_CONTROLLER, AnalysisController.class.getName(), false, true, "singleton"));
            extraNfItfs.add(tf.createGCMItfType(ACSManager.MONITORING_CONTROLLER, monClazz, false, true, "singleton"));

            // Add extra nf interfaces based of the f interfaces
            for (InterfaceType fItfType : fInterfaceTypes) {

                // client interfaces needs an external monitor connection
                if (fItfType.isFcClientItf()) {
                    String name = fItfType.getFcItfName() + RemoteMonitoringManager.REMOTE_MONITORING_SUFFIX;
                    if (isSingleton(fItfType)) {
                        extraNfItfs.add(tf.createGCMItfType(name, monClazz, true, true, "singleton"));
                    } else if (fItfType instanceof PAGCMInterfaceType) {
                        // gcm special interfaces
                        if (((PAGCMInterfaceType) fItfType).isGCMGathercastItf()) {
                            extraNfItfs.add(tf.createGCMItfType(name, monClazz, true, true, "singleton"));
                        }

                        // not supported for now
                        //
                        // if (((PAGCMInterfaceType) fItfType).isGCMMulticastItf()) {
                        //    extraNfItfs.add(tf.createGCMItfType(name, MulticastMonitoringController.class.getName(), true, true, "multicast"));
                        // }
                    }
                }

                // server interfaces needs an internal monitor connection
                else if (isSingleton(fItfType)) {
                    String name = fItfType.getFcItfName() + RemoteMonitoringManager.REMOTE_MONITORING_SUFFIX;
                    extraNfItfs.add(tf.createGCMItfType(name, monClazz, true, true, "singleton", true));
                }
            }
        } catch (InstantiationException e) {
            throw new ACSFactoryException("Couldn't create nf interface", e);
        }

        return extraNfItfs;
    }

    /**
     * Instantiates the component controllers and attaches them inside the host component membrane
     */
    public void addACSControllers(Component host) throws ACSFactoryException {

        PAMembraneController membrane;
        PAGCMLifeCycleController lifeCycle;

        try {
            membrane = Utils.getPAMembraneController(host);
            lifeCycle = Utils.getPAGCMLifeCycleController(host);
        } catch (NoSuchInterfaceException e) {
            String msg = "Membrane or LifeCycle interfaces not found on component " + host;
            logger.error(msg);
            throw new ACSFactoryException(msg, e);
        }

        // To verify if the component has a STOPPED life-cycle we need a STARTED membrane
        String membraneState = membrane.getMembraneState();
        if (membraneState.equals(PAMembraneController.MEMBRANE_STOPPED)) {
            startMembrane(host, "addACSControllers");
        }

        // Stopping everything
        String lifeCycleState = lifeCycle.getFcState();
        if (lifeCycleState.equals(PAGCMLifeCycleController.STARTED)) {
            stopLifeCycle(host, "addACSControllers");
        }
        if (membrane.getMembraneState().equals(PAMembraneController.MEMBRANE_STARTED)) {
            stopMembrane(host, "addACSControllers");
        }

        Node hostNode = null;
        try {
            hostNode = getNode(host);
        } catch (NodeException e) {
            e.printStackTrace();
        }

        addMonitoringController(membrane, hostNode, host);
        addAnalysisController(membrane, hostNode);
        addPlanningController(membrane, hostNode);
        addExecutionController(membrane, hostNode);

        bindControllers(membrane, host);

        // Restoring old states
        if (membraneState.equals(PAMembraneController.MEMBRANE_STARTED)) {
            startMembrane(host, "addACSControllers");
        }
        if (lifeCycleState.equals(PAGCMLifeCycleController.STARTED)) {
            startLifeCycle(host, "addACSControllers");
        }
    }

    private void addExecutionController(PAMembraneController membrane, Node hostNode) {
        try {
            Component executionController = ExecutionControllerFactory.getExecutionControllerInstance(tf, gf, hostNode);
            membrane.nfAddFcSubComponent(executionController);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addPlanningController(PAMembraneController membrane, Node hostNode) {
        try {
            Component planningController = PlanningControllerFactory.getPlanningControllerInstance(tf, gf, hostNode);
            membrane.nfAddFcSubComponent(planningController);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addAnalysisController(PAMembraneController membraneController, Node node) {
        try {
            Component analysisController = AnalysisControllerFactory.getAnalysisControllerInstance(tf, gf, node);
            membraneController.nfAddFcSubComponent(analysisController);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addMonitoringController(PAMembraneController membraneController, Node hostNode, Component host) {
        try {
            String hierarchy = ((PAComponent) host).getComponentParameters().getHierarchicalType();
            InterfaceType[] interfaceTypes = ((PAComponent) host).getComponentParameters().getInterfaceTypes();

            Component monitoringManager = MonitoringManagerFactory.getMonitoringManagerInstance(tf, gf, hostNode);
            Component eventListener = GCMPAEventListenerFactory.getGCMPAEventListenerInstance(tf, gf, hostNode);
            Component metricStore = MetricStoreFactory.getMetricStoreInstance(tf, gf, hostNode, interfaceTypes, hierarchy);
            Component recordStore = RecordStoreFactory.getRecordStoreInstance(tf, gf, hostNode);

            membraneController.nfAddFcSubComponent(monitoringManager);
            membraneController.nfAddFcSubComponent(eventListener);
            membraneController.nfAddFcSubComponent(metricStore);
            membraneController.nfAddFcSubComponent(recordStore);

            membraneController.nfBindFc(MonitoringManagerFactory.COMPONENT_NAME + "." + MetricStore.ITF_NAME,
                    MetricStoreFactory.COMPONENT_NAME + "." + MetricStore.ITF_NAME);

            membraneController.nfBindFc(MonitoringManagerFactory.COMPONENT_NAME + "." + RemoteMonitoringManager.ITF_NAME,
                    MetricStoreFactory.COMPONENT_NAME + "." + RemoteMonitoringManager.ITF_NAME);

            membraneController.nfBindFc(MonitoringManagerFactory.COMPONENT_NAME + "." + GCMPAEventListener.ITF_NAME,
                    GCMPAEventListenerFactory.COMPONENT_NAME + "." + GCMPAEventListener.ITF_NAME);

            membraneController.nfBindFc(GCMPAEventListenerFactory.COMPONENT_NAME + "." + RecordEventListener.ITF_NAME,
                    MetricStoreFactory.COMPONENT_NAME + "." + RecordEventListener.ITF_NAME);

            membraneController.nfBindFc(GCMPAEventListenerFactory.COMPONENT_NAME + "." + RecordStore.ITF_NAME,
                    RecordStoreFactory.COMPONENT_NAME + "." + RecordStore.ITF_NAME);

            membraneController.nfBindFc(MetricStoreFactory.COMPONENT_NAME + "." + RecordStore.ITF_NAME,
                    RecordStoreFactory.COMPONENT_NAME + "." + RecordStore.ITF_NAME);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void bindControllers(PAMembraneController m, Component host) throws ACSFactoryException {
        try {
            String hostName = getComponentName(host);

            //logger.trace("Binding monitoring on {}", hostName);
            m.nfBindFc(ACSManager.MONITORING_CONTROLLER,
                    MonitoringManagerFactory.COMPONENT_NAME + "." + MonitoringController.ITF_NAME);
            m.nfBindFc(MetricStoreFactory.COMPONENT_NAME + "." + MetricEventListener.ITF_NAME,
                    AnalysisControllerFactory.COMPONENT_NAME + "." + MetricEventListener.ITF_NAME);

            //logger.trace("Binding analysis on {}", hostName);
            m.nfBindFc(ACSManager.ANALYSIS_CONTROLLER,
                    AnalysisControllerFactory.COMPONENT_NAME + "." + AnalysisController.ITF_NAME);
            m.nfBindFc(AnalysisControllerFactory.COMPONENT_NAME + "." + MonitoringController.ITF_NAME,
                    MonitoringManagerFactory.COMPONENT_NAME + "." + MonitoringController.ITF_NAME);
            m.nfBindFc(AnalysisControllerFactory.COMPONENT_NAME + "." + RuleEventListener.ITF_NAME,
                    PlanningControllerFactory.COMPONENT_NAME + "." + RuleEventListener.ITF_NAME);

            //logger.trace("Binding planning on {}", hostName);
            m.nfBindFc(ACSManager.PLANNING_CONTROLLER,
                    PlanningControllerFactory.COMPONENT_NAME + "." + PlanningController.ITF_NAME);
            m.nfBindFc(PlanningControllerFactory.COMPONENT_NAME + "." + MonitoringController.ITF_NAME,
                    MonitoringManagerFactory.COMPONENT_NAME + "." + MonitoringController.ITF_NAME);
            m.nfBindFc(PlanningControllerFactory.COMPONENT_NAME + "." + ExecutionController.ITF_NAME,
                    ExecutionControllerImpl.CONTROLLER_NAME + "." + ExecutionController.ITF_NAME);

            //logger.trace("Binding execution on {}", hostName);
            m.nfBindFc(ACSManager.EXECUTION_CONTROLLER,
                    ExecutionControllerFactory.COMPONENT_NAME + "." + ExecutionController.ITF_NAME);
        }
        catch (NoSuchInterfaceException | IllegalLifeCycleException |
                IllegalBindingException | NoSuchComponentException e) {
            throw new ACSFactoryException("Controllers binding fail", e);
        }
    }


    /**
     * Returns the deployment node of the component
     */
    private Node getNode(Component component) throws NodeException {
        UniversalBodyProxy ubProxy = (UniversalBodyProxy) ((PAComponentRepresentative) component).getProxy();
        return NodeFactory.getNode(ubProxy.getBody().getNodeURL());
    }

    /**
     * Returns true if the interface is a singleton interface
     */
    private boolean isSingleton(InterfaceType itf) {
        boolean isSingleton = !itf.isFcCollectionItf();
        if (itf instanceof PAGCMInterfaceType) {
            isSingleton = isSingleton && ((PAGCMInterfaceType) itf).isGCMSingletonItf();
        }
        return isSingleton;
    }

    private String getComponentName(Component component) {
        if (component instanceof PAComponent) {
            return ((PAComponent) component).getComponentParameters().getName();
        }
        try {
            return GCM.getNameController(component).getFcName();
        } catch (NoSuchInterfaceException e) {
            return "*unknown*";
        }
    }

    private void startLifeCycle(Component component, String where)
            throws ACSFactoryException {
        try {
            //logger.debug("{}: starting the life-cycle on component {}", where, getComponentName(component));
            Utils.getPAGCMLifeCycleController(component).startFc();
        } catch (NoSuchInterfaceException | IllegalLifeCycleException e) {
            String msg = where + ": LifeCycle cannot be started on component " + getComponentName(component);
            logger.error(msg);
            throw new ACSFactoryException(msg, e);
        }
    }

    private void startMembrane(Component component, String where)
            throws ACSFactoryException {
        try {
            //logger.debug("{}: starting the membrane on component {}", where, getComponentName(component));
            Utils.getPAMembraneController(component).startMembrane();
        } catch (NoSuchInterfaceException | IllegalLifeCycleException e) {
            String msg = where + ": Membrane cannot be started on component " + getComponentName(component);
            logger.error(msg);
            throw new ACSFactoryException(msg, e);
        }
    }

    private void stopLifeCycle(Component component, String where)
            throws ACSFactoryException {
        try {
            //logger.debug("{}: stopping the life-cycle on component {}", where, getComponentName(component));
            Utils.getPAGCMLifeCycleController(component).stopFc();
        } catch (NoSuchInterfaceException | IllegalLifeCycleException e) {
            String msg = where + ": LifeCycle cannot be stopped on component " + getComponentName(component);
            logger.error(msg);
            throw new ACSFactoryException(msg, e);
        }
    }

    private void stopMembrane(Component component, String where)
            throws ACSFactoryException {
        try {
            //logger.debug("{}: stopping the membrane on component {}", where, getComponentName(component));
            Utils.getPAMembraneController(component).stopMembrane();
        } catch (NoSuchInterfaceException | IllegalLifeCycleException e) {
            String msg = where + ": Membrane cannot be stopped on component " + getComponentName(component);
            logger.error(msg);
            throw new ACSFactoryException(msg, e);
        }
    }
}
