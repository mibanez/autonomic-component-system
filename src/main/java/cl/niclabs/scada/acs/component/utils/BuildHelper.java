package cl.niclabs.scada.acs.component.utils;

import cl.niclabs.scada.acs.component.controllers.MonitorController;
import cl.niclabs.scada.acs.component.controllers.MonitorControllerImpl;
import cl.niclabs.scada.acs.component.controllers.MonitorControllerMulticast;
import cl.niclabs.scada.acs.component.controllers.exceptions.ACSIntegrationException;
import cl.niclabs.scada.acs.component.factory.ACSTypeFactory;
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

    public static final Logger logger = LoggerFactory.getLogger(BuildHelper.class);
    private static final String CONTROLLER_CONFIG = "/org/objectweb/proactive/core/component/componentcontroller/config/default-component-controller-config.xml";

    // Normally, the NF interfaces mentioned here should be those that are going to be implemented by NF component,
    // and the rest of the NF interfaces (that are going to be implemented by object controller) should be in a
    // ControllerDesc file. But the PAComponentImpl ignores the NFType if there is a ControllerDesc file specified,
    // so I better put all the NF interfaces here.
    // That means that I need another method to add the object controllers for the (not yet created) controllers.
    // -- cruz
    public static List<InterfaceType> getStandardNfInterfaces(PAGCMTypeFactory tf) throws InstantiationException {
        ArrayList<InterfaceType> nfInterfaces = new ArrayList<>();
        logger.info("generating standard nf interfaces...");
        nfInterfaces.add(tf.createGCMItfType(Constants.CONTENT_CONTROLLER, PAContentController.class.getName(), false, false, "singleton"));
        nfInterfaces.add(tf.createGCMItfType(Constants.BINDING_CONTROLLER, PABindingController.class.getName(), false, false, "singleton"));
        nfInterfaces.add(tf.createGCMItfType(Constants.LIFECYCLE_CONTROLLER, PAGCMLifeCycleController.class.getName(), false, false, "singleton"));
        nfInterfaces.add(tf.createGCMItfType(Constants.SUPER_CONTROLLER, PASuperController.class.getName(), false, false, "singleton"));
        nfInterfaces.add(tf.createGCMItfType(Constants.NAME_CONTROLLER, NameController.class.getName(), false, false, "singleton"));
        nfInterfaces.add(tf.createGCMItfType(Constants.MEMBRANE_CONTROLLER, PAMembraneController.class.getName(), false, false, "singleton"));
        nfInterfaces.add(tf.createGCMItfType(Constants.MULTICAST_CONTROLLER, PAMulticastController.class.getName(), false, true, "singleton"));
        return nfInterfaces;
    }

    // Adds the controller objects to the NF interfaces that are not part of the M&M Framework.
    // Normally, PAComponentImpl.addMandatoryControllers should have added already the mandatory
    // MEMBRANE, LIFECYCLE and NAME controllers. Interfaces like BINDING and CONTENT, which are not
    // supposed to be in all components, should have been removed from the component NFType in the appropriate cases.
    public static void addObjectControllers(Component component) throws ACSIntegrationException {
        if (!(component instanceof PAComponent)) {
            throw new ACSIntegrationException("the component must be a PAComponent instance");
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
    public static List<InterfaceType> getAcsNfInterfaces(PAGCMTypeFactory tf, InterfaceType[] fnInterfaceTypes)
            throws InstantiationException {

        logger.debug("getAcsNfInterfaces: adding acs nf interfaces...");
        ArrayList<InterfaceType> acsInterfaces = new ArrayList<>();
        acsInterfaces.add(tf.createGCMItfType("monitor-controller", MonitorController.class.getName(), false, true, "singleton"));

        for (InterfaceType itf : fnInterfaceTypes) {

            if (!itf.isFcClientItf() && isSingleton(itf)) {
                acsInterfaces.add(tf.createGCMItfType(itf.getFcItfName() + "-internal-monitor-controller",
                        MonitorController.class.getName(), true, true, "singleton", true));
                logger.debug("getAcsNfInterfaces: -- " + itf.getFcItfName() + "-internal-monitor-controller generated");
            }

            if (itf.isFcClientItf()) {
                if (isSingleton(itf)) {
                    acsInterfaces.add(tf.createGCMItfType(itf.getFcItfName() + "-external-monitor-controller",
                            MonitorController.class.getName(), true, true, "singleton"));
                    logger.debug("getAcsNfInterfaces: -- " + itf.getFcItfName() + "-external-monitor-controller generated [from singleton]");
                } else if (itf instanceof PAGCMInterfaceType) {
                    if (((PAGCMInterfaceType) itf).isGCMGathercastItf()) {
                        acsInterfaces.add(tf.createGCMItfType(itf.getFcItfName() + "-external-monitor-controller",
                                MonitorController.class.getName(), true, true, "singleton"));
                        logger.debug("getAcsNfInterfaces: -- " + itf.getFcItfName() + "-external-monitor-controller generated [from gathercast]");
                    } else if (((PAGCMInterfaceType) itf).isGCMMulticastItf()) {
                        acsInterfaces.add(tf.createGCMItfType(itf.getFcItfName() + "-external-monitor-controller",
                                MonitorControllerMulticast.class.getName(), true, true, "multicast"));
                        logger.debug("getAcsNfInterfaces: -- " + itf.getFcItfName() + "-external-monitor-controller generated [from multicast]");
                    }
                }
            }
        }

        acsInterfaces.add(tf.createGCMItfType("internal-server-monitor-controller",
                MonitorController.class.getName(), false, true, "singleton", true));
        logger.debug("getAcsNfInterfaces: -- internal-monitor-controller generated");

        return acsInterfaces;
    }

    /**
     * Instantiates the component controllers and attaches them inside the host component membrane
     */
    public static void addACSControllers(PAGCMTypeFactory tf, PAGenericFactory gf, Component host)
            throws ACSIntegrationException {

        // component name for debugging purposes
        String hostName = ACSUtils.getComponentName(host);

        PAMembraneController membrane;
        PAGCMLifeCycleController lifeCycle;

        try {
            membrane = Utils.getPAMembraneController(host);
            lifeCycle = Utils.getPAGCMLifeCycleController(host);
        } catch (NoSuchInterfaceException e) {
            String msg = "Membrane or LifeCycle interfaces not found on component " + host;
            logger.error(msg);
            throw new ACSIntegrationException(msg, e);
        }

        // To verify if the component has a STOPPED life-cycle we need a STARTED membrane
        String membraneState = membrane.getMembraneState();
        if (membraneState.equals(PAMembraneController.MEMBRANE_STOPPED)) {
            ACSUtils.startMembrane(membrane, "addACSControllers", hostName);
        }

        // Stopping everything
        String lifeCycleState = lifeCycle.getFcState();
        if (lifeCycleState.equals(PAGCMLifeCycleController.STARTED)) {
            ACSUtils.stopLifeCycle(lifeCycle, "addACSControllers", hostName);
        }
        if (membrane.getMembraneState().equals(PAMembraneController.MEMBRANE_STARTED)) {
            ACSUtils.stopMembrane(membrane, "addACSControllers", hostName);
        }

        addMonitor(tf, gf, membrane, host);
        bindControllers(membrane, host);

        // Restoring old states
        if (membraneState.equals(PAMembraneController.MEMBRANE_STARTED)) {
            ACSUtils.startMembrane(membrane, "addACSControllers", hostName);
        }
        if (lifeCycleState.equals(PAGCMLifeCycleController.STARTED)) {
            ACSUtils.startLifeCycle(lifeCycle, "addACSControllers", hostName);
        }
    }


    private static void bindControllers(PAMembraneController membrane, Component host) throws ACSIntegrationException {
        try {
            logger.debug("bindingControllers: binding controllers on component {}", ACSUtils.getComponentName(host));
            membrane.nfBindFc("monitor-controller", "MonitorController.monitor-controller-nf");
        } catch (NoSuchInterfaceException | IllegalLifeCycleException |
                IllegalBindingException | NoSuchComponentException e) {
            throw new ACSIntegrationException("Controllers binding fail", e);
        }
    }

    /**
     * Instantiates MonitorController and attaches it to the host component membrane
     */
    private static void addMonitor(PAGCMTypeFactory tf, PAGenericFactory gf, PAMembraneController m, Component host)
            throws ACSIntegrationException {

        logger.debug("addMonitor: adding monitor controller on component {}", ACSUtils.getComponentName(host));
        try {
            m.nfAddFcSubComponent(gf.newNfFcInstance(
                    getMonitorComponentType(tf, host),
                    new ControllerDescription("MonitorController", Constants.PRIMITIVE, CONTROLLER_CONFIG),
                    new ContentDescription(MonitorControllerImpl.class.getName()),
                    getNode(host)));
        } catch (InstantiationException e) {
            throw new ACSIntegrationException("ComponentType generation for monitor controller fail", e);
        } catch (NodeException e) {
            throw new ACSIntegrationException("Couldn't get the host component deployment node", e);
        } catch (IllegalLifeCycleException | IllegalContentException e) {
            throw new ACSIntegrationException("Couldn't add the monitor controller into the host component", e);
        }
    }

    /**
     * Generates a monitor controller ComponentType based on the host component attributes
     */
    private static ComponentType getMonitorComponentType(PAGCMTypeFactory tf, Component host)
            throws InstantiationException {

        ArrayList<InterfaceType> monItfTypes = new ArrayList<>();
        monItfTypes.add(tf.createGCMItfType("monitor-controller-nf", MonitorController.class.getName(),
                false, false, "singleton"));

        // external client interfaces
        for (InterfaceType itf : ((ComponentType) host.getFcType()).getFcInterfaceTypes()) {
            if (!itf.isFcClientItf()) {
                continue;
            }
            if (isSingleton(itf)) {
                monItfTypes.add(tf.createGCMItfType(itf.getFcItfName() + "-external-monitor-controller-nf",
                        MonitorController.class.getName(), true, true, "singleton"));
            } else if (itf instanceof PAGCMInterfaceType) {
                if (((PAGCMInterfaceType) itf).isGCMGathercastItf()) {
                    monItfTypes.add(tf.createGCMItfType(itf.getFcItfName() + "-external-monitor-controller-nf",
                            MonitorController.class.getName(), true, true, "singleton"));
                } else if (((PAGCMInterfaceType) itf).isGCMMulticastItf()) {
                    monItfTypes.add(tf.createGCMItfType(itf.getFcItfName() + "-external-monitor-controller-nf",
                            MonitorControllerMulticast.class.getName(), true, true, "singleton")); // TODO: Singleton??

                }
            }
        }

        // composites also require internal client interfaces
        if (Constants.COMPOSITE.equals(((PAComponent) host).getComponentParameters().getHierarchicalType())) {
            for (InterfaceType itf : ((ComponentType) host.getFcType()).getFcInterfaceTypes()) {
                if (!itf.isFcClientItf() && isSingleton(itf)) {
                    monItfTypes.add(tf.createGCMItfType(itf.getFcItfName() + "-internal-monitor-controller-nf",
                            MonitorController.class.getName(), true, true, "singleton"));
                }
            }
        }

        if (tf instanceof ACSTypeFactory) {
            // ACSTypeFactory adds nf extra interfaces by default, so we use the createNfFcType() method instead
            return ((ACSTypeFactory) tf).createNfFcType(monItfTypes.toArray(new InterfaceType[monItfTypes.size()]));
        }

        return tf.createFcType(monItfTypes.toArray(new InterfaceType[monItfTypes.size()]));
    }

    /**
     * Returns the deployment node of the component
     */
    private static Node getNode(Component component) throws NodeException {
        UniversalBodyProxy ubProxy = (UniversalBodyProxy) ((PAComponentRepresentative) component).getProxy();
        return NodeFactory.getNode(ubProxy.getBody().getNodeURL());
    }

    /**
     * Returns true if the interface is a singleton interface
     */
    private static boolean isSingleton(InterfaceType itf) {
        boolean isSingleton = !itf.isFcCollectionItf();
        if (itf instanceof PAGCMInterfaceType) {
            isSingleton = isSingleton && ((PAGCMInterfaceType) itf).isGCMSingletonItf();
        }
        return isSingleton;
    }

}
