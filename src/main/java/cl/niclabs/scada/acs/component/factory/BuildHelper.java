package cl.niclabs.scada.acs.component.factory;

import cl.niclabs.scada.acs.component.ACSUtils;
import cl.niclabs.scada.acs.component.controllers.AnalysisController;
import cl.niclabs.scada.acs.component.controllers.ExecutionController;
import cl.niclabs.scada.acs.component.controllers.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.PlanningController;
import cl.niclabs.scada.acs.component.controllers.analysis.AnalysisControllerImpl;
import cl.niclabs.scada.acs.component.controllers.analysis.RuleEventListener;
import cl.niclabs.scada.acs.component.controllers.execution.ExecutionControllerImpl;
import cl.niclabs.scada.acs.component.controllers.monitoring.MetricEventListener;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringControllerImpl;
import cl.niclabs.scada.acs.component.controllers.planning.PlanningControllerImpl;
import cl.niclabs.scada.acs.component.factory.exceptions.ACSFactoryException;
import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalContentException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.NameController;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.body.proxy.UniversalBodyProxy;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mibanez
 */
public class BuildHelper {

    private static final Logger logger = LoggerFactory.getLogger(BuildHelper.class);
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

    // Normally, the NF interfaces mentioned here should be those that are going to be implemented by NF component,
    // and the rest of the NF interfaces (that are going to be implemented by object controller) should be in a
    // ControllerDesc file. But the PAComponentImpl ignores the NFType if there is a ControllerDesc file specified,
    // so I better put all the NF interfaces here.
    // That means that I need another method to add the object controllers for the (not yet created) controllers.
    // -- cruz
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

    // Adds the controller objects to the NF interfaces that are not part of the M&M Framework.
    // Normally, PAComponentImpl.addMandatoryControllers should have added already the mandatory
    // MEMBRANE, LIFECYCLE and NAME controllers. Interfaces like BINDING and CONTENT, which are not
    // supposed to be in all components, should have been removed from the component NFType in the appropriate cases.
    public void addObjectControllers(Component component) throws ACSFactoryException {

        if (!(component instanceof PAComponent)) {
            throw new ACSFactoryException("the component must be a PAComponent instance");
        }

        logger.debug("addObjectControllers: adding object controllers on {}",
                ((PAComponent) component).getComponentParameters().getName());

        PAMembraneController membrane;
        try {
            membrane = Utils.getPAMembraneController(component);
        } catch (NoSuchInterfaceException e) {
            // Non-existent interfaces have been ignored at component creation time.
            return;
        }
        // add the remaining object controllers
        try {
            // this call is just to catch the exception. If the exception is generated in the next line
            // for some reason I can't catch it here.
            component.getFcInterface(Constants.CONTENT_CONTROLLER);
            membrane.setControllerObject(Constants.CONTENT_CONTROLLER, PAContentControllerImpl.class.getName());
        } catch (NoSuchInterfaceException e) {
            // Non-existent interfaces have been ignored at component creation time.
        }
        try {
            component.getFcInterface(Constants.BINDING_CONTROLLER);
            membrane.setControllerObject(Constants.BINDING_CONTROLLER, PABindingControllerImpl.class.getName());
        } catch (NoSuchInterfaceException e) {
            // Non-existent interfaces have been ignored at component creation time.
        }
        try {
            component.getFcInterface(Constants.SUPER_CONTROLLER);
            membrane.setControllerObject(Constants.SUPER_CONTROLLER, PASuperControllerImpl.class.getName());
        } catch (NoSuchInterfaceException e) {
            // Non-existent interfaces have been ignored at component creation time.
        }
        try {
            component.getFcInterface(Constants.MULTICAST_CONTROLLER);
            membrane.setControllerObject(Constants.MULTICAST_CONTROLLER, PAMulticastControllerImpl.class.getName());
        } catch (NoSuchInterfaceException e) {
            // Non-existent interfaces have been ignored at component creation time.
        }
        // LIFECYCLE is mandatory and should have been added at component creation time
        // NAME      is mandatory and should have been added at component creation time
    }

    /**
     * Generates the extra NF interfaces needed by every ACS component.
     */
    public List<InterfaceType> getAcsNfInterfaces(InterfaceType[] fnInterfaceTypes)
            throws ACSFactoryException {

        String monClazz = MonitoringController.class.getName();
        ArrayList<InterfaceType> acsInterfaces = new ArrayList<>();

        try {
            // Add controller main nf interface
            acsInterfaces.add(tf.createGCMItfType(ACSUtils.EXECUTION_CONTROLLER, ExecutionController.class.getName(), false, true, "singleton"));
            acsInterfaces.add(tf.createGCMItfType(ACSUtils.PLANNING_CONTROLLER, PlanningController.class.getName(), false, true, "singleton"));
            acsInterfaces.add(tf.createGCMItfType(ACSUtils.ANALYSIS_CONTROLLER, AnalysisController.class.getName(), false, true, "singleton"));
            acsInterfaces.add(tf.createGCMItfType(ACSUtils.MONITORING_CONTROLLER, monClazz, false, true, "singleton"));

            // Add extra nf interfaces based of the f interfaces
            for (InterfaceType itf : fnInterfaceTypes) {

                if (itf.isFcClientItf()) {
                    // client interfaces needs an external monitor connection
                    String name = String.format("%s-external-monitor-controller", itf.getFcItfName());

                    if (isSingleton(itf)) {
                        acsInterfaces.add(tf.createGCMItfType(name, monClazz, true, true, "singleton"));
                    } else if (itf instanceof PAGCMInterfaceType) {
                        // gcm special interfaces
                        if (((PAGCMInterfaceType) itf).isGCMGathercastItf()) {
                            acsInterfaces.add(tf.createGCMItfType(name, monClazz, true, true, "singleton"));
                        }
                        if (((PAGCMInterfaceType) itf).isGCMMulticastItf()) {
                            acsInterfaces.add(tf.createGCMItfType(name, monClazz, true, true, "multicast"));
                        }
                    }
                } else if (isSingleton(itf)) {
                    // server interfaces needs an internal monitor connection
                    String name = String.format("%s-internal-monitor-controller", itf.getFcItfName());
                    acsInterfaces.add(tf.createGCMItfType(name, monClazz, true, true, "singleton", true));
                }
            }

            // composite components needs and internal server monitor connection [does it?]
            String name = "internal-server-monitor-controller";
            acsInterfaces.add(tf.createGCMItfType(name, monClazz, false, true, "singleton", true));

        } catch (InstantiationException e) {
            throw new ACSFactoryException("Couldn't create nf interface", e);
        }

        return acsInterfaces;
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

        addController(ExecutionControllerImpl.CONTROLLER_NAME, ExecutionControllerImpl.class.getName(), getExecutionComponentType(), host);
        addController(PlanningControllerImpl.CONTROLLER_NAME, PlanningControllerImpl.class.getName(), getPlanningComponentType(), host);
        addController(AnalysisControllerImpl.CONTROLLER_NAME, AnalysisControllerImpl.class.getName(), getAnalysisComponentType(), host);
        addController(MonitoringControllerImpl.CONTROLLER_NAME, MonitoringControllerImpl.class.getName(), getMonitorComponentType(host), host);
        bindControllers(host);

        // Restoring old states
        if (membraneState.equals(PAMembraneController.MEMBRANE_STARTED)) {
            startMembrane(host, "addACSControllers");
        }
        if (lifeCycleState.equals(PAGCMLifeCycleController.STARTED)) {
            startLifeCycle(host, "addACSControllers");
        }
    }


    private void bindControllers(Component host) throws ACSFactoryException {
        try {
            PAMembraneController m = Utils.getPAMembraneController(host);
            String hostName = getComponentName(host);

            logger.trace("Binding monitoring on {}", hostName);
            m.nfBindFc(ACSUtils.MONITORING_CONTROLLER, MonitoringControllerImpl.CONTROLLER_NAME + "." + MonitoringControllerImpl.MONITORING_CONTROLLER_SERVER_ITF);
            m.nfBindFc(MonitoringControllerImpl.CONTROLLER_NAME + "." + MonitoringControllerImpl.METRIC_EVENT_LISTENER_CLIENT_ITF,
                    AnalysisControllerImpl.CONTROLLER_NAME + "." + AnalysisControllerImpl.METRIC_EVENT_LISTENER_SERVER_ITF);

            logger.trace("Binding analysis on {}", hostName);
            m.nfBindFc(ACSUtils.ANALYSIS_CONTROLLER, AnalysisControllerImpl.CONTROLLER_NAME + "." + AnalysisControllerImpl.ANALYSIS_CONTROLLER_SERVER_ITF);
            m.nfBindFc(AnalysisControllerImpl.CONTROLLER_NAME + "." + AnalysisControllerImpl.MONITORING_CONTROLLER_CLIENT_ITF,
                    MonitoringControllerImpl.CONTROLLER_NAME + "." + MonitoringControllerImpl.MONITORING_CONTROLLER_SERVER_ITF);
            m.nfBindFc(AnalysisControllerImpl.CONTROLLER_NAME + "." + AnalysisControllerImpl.RULE_EVENT_LISTENER_CLIENT_ITF,
                    PlanningControllerImpl.CONTROLLER_NAME + "." + PlanningControllerImpl.RULE_EVENT_LISTENER_SERVER_ITF);

            logger.trace("Binding planning on {}", hostName);
            m.nfBindFc(ACSUtils.PLANNING_CONTROLLER, PlanningControllerImpl.CONTROLLER_NAME + "." + PlanningControllerImpl.PLANNING_CONTROLLER_SERVER_ITF);
            m.nfBindFc(PlanningControllerImpl.CONTROLLER_NAME + "." + PlanningControllerImpl.MONITORING_CONTROLLER_CLIENT_ITF,
                    MonitoringControllerImpl.CONTROLLER_NAME + "." + MonitoringControllerImpl.MONITORING_CONTROLLER_SERVER_ITF);
            m.nfBindFc(PlanningControllerImpl.CONTROLLER_NAME + "." + PlanningControllerImpl.EXECUTION_CONTROLLER_CLIENT_ITF,
                    ExecutionControllerImpl.CONTROLLER_NAME + "." + ExecutionControllerImpl.EXECUTION_CONTROLLER_SERVER_ITF);

            logger.trace("Binding execution on {}", hostName);
            m.nfBindFc(ACSUtils.EXECUTION_CONTROLLER, ExecutionControllerImpl.CONTROLLER_NAME + "." + ExecutionControllerImpl.EXECUTION_CONTROLLER_SERVER_ITF);
        } catch (NoSuchInterfaceException | IllegalLifeCycleException |
                IllegalBindingException | NoSuchComponentException e) {
            throw new ACSFactoryException("Controllers binding fail", e);
        }
    }

    private void addController(String name, String className, ComponentType componentType, Component host) throws ACSFactoryException {
        try {
            Utils.getPAMembraneController(host).nfAddFcSubComponent(gf.newNfFcInstance(
                    componentType,
                    new ControllerDescription(name, Constants.PRIMITIVE, CONTROLLER_CONFIG),
                    new ContentDescription(className),
                    getNode(host)
            ));
        } catch (InstantiationException e) {
            throw new ACSFactoryException("Fail when instantiating controller", e);
        } catch (NodeException e) {
            throw new ACSFactoryException("Fail when getting the deployment node for controller", e);
        } catch (IllegalLifeCycleException | IllegalContentException e) {
            throw new ACSFactoryException("Fail when adding controller to its host component", e);
        } catch (NoSuchInterfaceException e) {
            throw new ACSFactoryException("Fail when getting the host component membrane controller", e);
        }
    }

    private ComponentType getExecutionComponentType() throws ACSFactoryException {
        try {
            ArrayList<InterfaceType> itfTypes = new ArrayList<>();
            itfTypes.add(tf.createGCMItfType(ExecutionControllerImpl.EXECUTION_CONTROLLER_SERVER_ITF,
                    ExecutionController.class.getName(), false, false, "singleton"));

            return tf.createFcType(itfTypes.toArray(new InterfaceType[itfTypes.size()]));
        } catch (InstantiationException e) {
            throw new ACSFactoryException("Fail when instantiating an interface for execution controller", e);
        }
    }

    private ComponentType getPlanningComponentType() throws ACSFactoryException {
        try {
            ArrayList<InterfaceType> itfTypes = new ArrayList<>();
            itfTypes.add(tf.createGCMItfType(PlanningControllerImpl.PLANNING_CONTROLLER_SERVER_ITF,
                    PlanningController.class.getName(), false, false, "singleton"));
            itfTypes.add(tf.createGCMItfType(PlanningControllerImpl.RULE_EVENT_LISTENER_SERVER_ITF,
                    RuleEventListener.class.getName(), false, false, "singleton"));
            itfTypes.add(tf.createGCMItfType(PlanningControllerImpl.MONITORING_CONTROLLER_CLIENT_ITF,
                    MonitoringController.class.getName(), true, false, "singleton"));
            itfTypes.add(tf.createGCMItfType(PlanningControllerImpl.EXECUTION_CONTROLLER_CLIENT_ITF,
                    ExecutionController.class.getName(), true, false, "singleton"));

            return tf.createFcType(itfTypes.toArray(new InterfaceType[itfTypes.size()]));
        } catch (InstantiationException e) {
            throw new ACSFactoryException("Fail when instantiating an interface for planning controller", e);
        }
    }


    private ComponentType getAnalysisComponentType() throws ACSFactoryException {
        try {
            ArrayList<InterfaceType> itfTypes = new ArrayList<>();
            itfTypes.add(tf.createGCMItfType(AnalysisControllerImpl.ANALYSIS_CONTROLLER_SERVER_ITF,
                    AnalysisController.class.getName(), false, false, "singleton"));
            itfTypes.add(tf.createGCMItfType(AnalysisControllerImpl.METRIC_EVENT_LISTENER_SERVER_ITF,
                    MetricEventListener.class.getName(), false, false, "singleton"));
            itfTypes.add(tf.createGCMItfType(AnalysisControllerImpl.MONITORING_CONTROLLER_CLIENT_ITF,
                    MonitoringController.class.getName(), true, false, "singleton"));
            itfTypes.add(tf.createGCMItfType(AnalysisControllerImpl.RULE_EVENT_LISTENER_CLIENT_ITF,
                    RuleEventListener.class.getName(), true, false, "singleton"));

            return tf.createFcType(itfTypes.toArray(new InterfaceType[itfTypes.size()]));
        } catch (InstantiationException e) {
            throw new ACSFactoryException("Fail when instantiating an interface for analysis controller", e);
        }
    }

    /**
     * Generates a monitor controller ComponentType based on the host component attributes
     */
    private ComponentType getMonitorComponentType(Component host) throws ACSFactoryException {

        String monClazz = MonitoringController.class.getName();
        ArrayList<InterfaceType> monItfTypes = new ArrayList<>();

        try {
            // Add monitor-controller main nf interfaces
            monItfTypes.add(tf.createGCMItfType(MonitoringControllerImpl.MONITORING_CONTROLLER_SERVER_ITF, monClazz, false, false, "singleton"));
            monItfTypes.add(tf.createGCMItfType(MonitoringControllerImpl.METRIC_EVENT_LISTENER_CLIENT_ITF,
                    MetricEventListener.class.getName(), true, false, "singleton"));

            // Add extra nf interfaces based of the host's f interfaces
            for (InterfaceType itf : ((ComponentType) host.getFcType()).getFcInterfaceTypes()) {

                // Ignore server interfaces
                if (!itf.isFcClientItf()) {
                    continue;
                }

                // Add communication with the external monitors
                String name = itf.getFcItfName() + "-external-monitor-controller-nf";

                if (isSingleton(itf)) {
                    monItfTypes.add(tf.createGCMItfType(name, monClazz, true, true, "singleton"));
                } else if (itf instanceof PAGCMInterfaceType) {
                    // gcm special interfaces
                    if (((PAGCMInterfaceType) itf).isGCMGathercastItf()) {
                        monItfTypes.add(tf.createGCMItfType(name, monClazz, true, true, "singleton"));
                    } else if (((PAGCMInterfaceType) itf).isGCMMulticastItf()) {
                        monItfTypes.add(tf.createGCMItfType(name, monClazz, true, true, "singleton"));
                        // TODO: Singleton??
                    }
                }
            }

            // Composite also require internal client interfaces communication
            if (Constants.COMPOSITE.equals(((PAComponent) host).getComponentParameters().getHierarchicalType())) {
                for (InterfaceType itf : ((ComponentType) host.getFcType()).getFcInterfaceTypes()) {
                    if (!itf.isFcClientItf() && isSingleton(itf)) {
                        String name = itf.getFcItfName() + "-internal-monitor-controller-nf";
                        monItfTypes.add(tf.createGCMItfType(name, monClazz, true, true, "singleton"));
                    }
                }
            }

        } catch (InstantiationException e) {
            throw new ACSFactoryException("Couldn't create interface for the monitor controller", e);
        }

        try {
            // Create the monitor controller component Type
            return tf.createFcType(monItfTypes.toArray(new InterfaceType[monItfTypes.size()]));
        } catch (InstantiationException e) {
            throw new ACSFactoryException("Couldn't create monitor controller component type", e);
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
            logger.debug("{}: starting the life-cycle on component {}", where, getComponentName(component));
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
            logger.debug("{}: starting the membrane on component {}", where, getComponentName(component));
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
            logger.debug("{}: stopping the life-cycle on component {}", where, getComponentName(component));
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
            logger.debug("{}: stopping the membrane on component {}", where, getComponentName(component));
            Utils.getPAMembraneController(component).stopMembrane();
        } catch (NoSuchInterfaceException | IllegalLifeCycleException e) {
            String msg = where + ": Membrane cannot be stopped on component " + getComponentName(component);
            logger.error(msg);
            throw new ACSFactoryException(msg, e);
        }
    }
}
