package cl.niclabs.scada.acs.component;

import cl.niclabs.scada.acs.component.controllers.analysis.AnalysisController;
import cl.niclabs.scada.acs.component.controllers.execution.ExecutionController;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.MetricStoreFactory;
import cl.niclabs.scada.acs.component.controllers.planning.PlanningController;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.PAInterface;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.control.PAMembraneController;
import org.objectweb.proactive.core.component.control.PAMulticastController;
import org.objectweb.proactive.core.component.exceptions.NoSuchComponentException;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.core.component.representative.PAComponentRepresentative;
import org.objectweb.proactive.core.component.type.PAGCMInterfaceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static cl.niclabs.scada.acs.component.controllers.monitoring.metrics.RemoteMonitoringManager.INTERNAL_MONITORING_SUFFIX;
import static cl.niclabs.scada.acs.component.controllers.monitoring.metrics.RemoteMonitoringManager.REMOTE_MONITORING_SUFFIX;

/**
 * Utility methods for BuildHelper
 *
 * Created by mibanez
 */
public class ACSUtils {

    private static final Logger logger = LoggerFactory.getLogger(ACSUtils.class);

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

        if ( !(component instanceof PAComponent) ) {
            throw new ACSException("The component must be a PAComponent instance",
                    new ClassCastException(String.format("Component %s can't be cast to %s",
                            component.getClass().getName(), PAComponent.class.getName())));
        }

        PAComponent pacomponent = (PAComponent) component;
        String componentName = pacomponent.getComponentParameters().getName();
        logger.trace("enabling remote monitoring from {}", componentName);

        InterfaceType[] interfaceTypes = pacomponent.getComponentParameters().getInterfaceTypes();
        boolean isComposite = pacomponent.getComponentParameters().getHierarchicalType().equals(Constants.COMPOSITE);
        Component parent = getParent(component);

        // Get essential Controllers
        PAMembraneController membraneController;
        try {
            membraneController = Utils.getPAMembraneController(component);
        } catch (NoSuchInterfaceException e) {
            throw new ACSException("Can't get essential controllers", e);
        }

        for(InterfaceType itfType : interfaceTypes) {

            PAGCMInterfaceType gcmItfType = (PAGCMInterfaceType) itfType;
            boolean isSingleton = gcmItfType.isGCMSingletonItf() && ! gcmItfType.isGCMCollectiveItf();

            if (isComposite && isSingleton && !gcmItfType.isFcClientItf()) {
                logger.trace("[enabling {}] on composite: server singleton itf {}", componentName, gcmItfType.getFcItfName());
                if (isAlreadyBound(gcmItfType.getFcItfName() + REMOTE_MONITORING_SUFFIX, membraneController)) {
                    continue; // break loops
                }
                Component destinationComp = getServerComponent(component, gcmItfType.getFcItfName());
                enableInternalServerItf(gcmItfType, destinationComp, membraneController);
                enableRemoteMonitoring(destinationComp);
            }

            if (!itfType.isFcClientItf()) {
                continue;
            }

            if (isSingleton || gcmItfType.isGCMGathercastItf()) {
                logger.trace("[enabling {}] on primitive: client singleton/gathercast itf {}", componentName, gcmItfType.getFcItfName());
                if (isAlreadyBound(gcmItfType.getFcItfName() + REMOTE_MONITORING_SUFFIX, membraneController)) {
                    continue; // break loops
                }
                Component destinationComp = getServerComponent(component, gcmItfType.getFcItfName());
                // ignore not PAComponentRepresentative (WSComponent for example)
                if ( !(destinationComp instanceof PAComponentRepresentative) ) {
                    continue;
                }
                boolean isParent = destinationComp.equals(parent);
                System.out.println("---enabling external client itf " + gcmItfType.getFcItfName());
                enableExternalClientItf(gcmItfType, destinationComp, isParent, membraneController);
                if (!isParent) {
                    enableRemoteMonitoring(destinationComp);
                }
            }

            if (gcmItfType.isGCMMulticastItf()) {
                logger.trace("[enabling {}] on primitive: client multicast itf {}", componentName, gcmItfType.getFcItfName());
                try {
                    PAMulticastController multicastController = Utils.getPAMulticastController(pacomponent);

                    // Remove old NF destinations
                    String nfItfName = gcmItfType.getFcItfName() + REMOTE_MONITORING_SUFFIX;
                    try {
                        for (Object nfItf : multicastController.lookupGCMMulticast(nfItfName)) {
                            multicastController.unbindGCMMulticast(nfItfName, nfItf);
                        }
                    } catch (IllegalBindingException | IllegalLifeCycleException e) {
                        throw new ACSException("Can't unbind old multicast binding for " + nfItfName, e);
                    }

                    // Get destination components
                    ArrayList<Component> destinations = new ArrayList<>();
                    for (Object destinationItf : multicastController.lookupGCMMulticast(gcmItfType.getFcItfName())) {
                        destinations.add(((PAInterface) destinationItf).getFcItfOwner());
                    }

                    String controllerItfName = gcmItfType.getFcItfName() + INTERNAL_MONITORING_SUFFIX;
                    String hostItfName = gcmItfType.getFcItfName() + REMOTE_MONITORING_SUFFIX;
                    try {
                        membraneController.stopMembrane();
                        membraneController.nfBindFc(MetricStoreFactory.COMPONENT_NAME + "." + controllerItfName, hostItfName);
                        membraneController.startMembrane();
                    } catch (IllegalLifeCycleException | NoSuchComponentException | IllegalBindingException e) {
                        throw new ACSException("Can't bind monitoring nf itfs for " + hostItfName, e);
                    }

                    for(Component destinationComp : destinations) {
                        //  ignore not PAComponentRepresentative (WSComponent for example)
                        if (!(destinationComp instanceof PAComponentRepresentative)) {
                            continue;
                        }
                        boolean isParent = destinationComp.equals(parent);
                        MonitoringController remoteMC;
                        try {
                            if (isParent) {
                                remoteMC = (MonitoringController) destinationComp.getFcInterface(
                                        itfType.getFcItfName() + REMOTE_MONITORING_SUFFIX);
                            } else {
                                remoteMC = getMonitoringController(destinationComp);
                            }
                        } catch (NoSuchInterfaceException e) {
                            throw new ACSException(String.format("Can't get remote monitoring controller for \"%s\" interface",
                                    itfType.getFcItfName()), e);
                        }

                        try {
                            membraneController.stopMembrane();
                            membraneController.nfBindFc(hostItfName, remoteMC);
                            membraneController.startMembrane();
                        } catch (IllegalLifeCycleException | NoSuchComponentException | IllegalBindingException e) {
                            throw new ACSException("Can't bind multicast monitoring controller for " + hostItfName, e);
                        }

                        if (!isParent) {
                            enableRemoteMonitoring(destinationComp);
                        }
                    }
                } catch (NoSuchInterfaceException e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
    }

    private static void enableInternalServerItf(PAGCMInterfaceType itfType, Component destinationComp,
                                                PAMembraneController membraneController) throws ACSException {
        MonitoringController remoteMC;
        try {
            remoteMC = getMonitoringController(destinationComp);
        } catch (NoSuchInterfaceException e) {
            throw new ACSException(String.format("Can't get remote monitoring controller for \"%s\" interface",
                    itfType.getFcItfName()), e);
        }

        String controllerItfName = itfType.getFcItfName() + INTERNAL_MONITORING_SUFFIX;
        String hostItfName = itfType.getFcItfName() + REMOTE_MONITORING_SUFFIX;
        try {
            membraneController.stopMembrane();
            membraneController.nfBindFc(MetricStoreFactory.COMPONENT_NAME + "." + controllerItfName, hostItfName);
            membraneController.nfBindFc(hostItfName, remoteMC);
            membraneController.startMembrane();
        } catch (NoSuchInterfaceException | NoSuchComponentException | IllegalBindingException | IllegalLifeCycleException e) {
            throw new ACSException("Can't bind monitoring nf itfs for " + hostItfName, e);
        }
    }

    private static void enableExternalClientItf(PAGCMInterfaceType itfType, Component destinationComp,
                                                boolean isParent, PAMembraneController membraneController) throws ACSException {
        MonitoringController remoteMC;
        try {
            if (isParent) {
                remoteMC = (MonitoringController) destinationComp.getFcInterface(
                        itfType.getFcItfName() + REMOTE_MONITORING_SUFFIX);
            } else {
                remoteMC = getMonitoringController(destinationComp);
            }
        } catch (NoSuchInterfaceException e) {
            throw new ACSException(String.format("Can't get remote monitoring controller for \"%s\" interface",
                    itfType.getFcItfName()), e);
        }

        String controllerItfName = itfType.getFcItfName() + INTERNAL_MONITORING_SUFFIX;
        String hostItfName = itfType.getFcItfName() + REMOTE_MONITORING_SUFFIX;
        try {
            membraneController.stopMembrane();
            membraneController.nfBindFc(MetricStoreFactory.COMPONENT_NAME + "." + controllerItfName, hostItfName);
            membraneController.nfBindFc(hostItfName, remoteMC);
            membraneController.startMembrane();
        } catch(Exception e) {
            throw new ACSException("Can't bind monitoring nf itfs for " + hostItfName, e);
        }
    }

    private static Component getParent(Component component) throws ACSException {
        try {
            Component[] parents = Utils.getPASuperController(component).getFcSuperComponents();
            if(parents.length > 0) {
                // we should get only one parent here, as GCM does not support shared components
                return ((PAComponent)parents[0]);
            }
            return null;
        } catch (NoSuchInterfaceException e) {
            throw new ACSException("Can't get parent component", e);
        }
    }

    private static boolean isAlreadyBound(String interfaceName, PAMembraneController membrane) {
        try {
            return membrane.nfLookupFc(interfaceName) != null;
        } catch (NoSuchInterfaceException | NoSuchComponentException ignore) {
        }
        return false;
    }

    private static Component getServerComponent(Component clientcomponent, String clientItfName) throws ACSException {
        try {
            Object serverItf = Utils.getPABindingController(clientcomponent).lookupFc(clientItfName);
            if (serverItf == null) {
                throw new NullPointerException();
            }
            return ((PAInterface) serverItf).getFcItfOwner();
        } catch(NoSuchInterfaceException | NullPointerException e) {
            throw new ACSException(String.format("Can't find the \"%s\" server component", clientItfName), e);
        }
    }
}
