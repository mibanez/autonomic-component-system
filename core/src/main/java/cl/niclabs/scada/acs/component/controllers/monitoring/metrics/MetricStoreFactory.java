package cl.niclabs.scada.acs.component.controllers.monitoring.metrics;


import cl.niclabs.scada.acs.component.ACSManager;
import cl.niclabs.scada.acs.component.body.ACSComponentRunActive;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.monitoring.events.RecordEventListener;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.RecordStore;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.type.PAGCMInterfaceType;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.multiactivity.component.ComponentMultiActiveService;

import java.util.ArrayList;

public class MetricStoreFactory {

    public static final String COMPONENT_NAME = "MetricStore";

    public static Component getMetricStoreInstance(PAGCMTypeFactory typeF, PAGenericFactory genericF, Node host,
                                                   InterfaceType[] functionalItfTypes, String hierarchy)
            throws org.objectweb.fractal.api.factory.InstantiationException {

        return genericF.newNfFcInstance(
                getComponentType(typeF, functionalItfTypes, hierarchy),
                getControllerDescription(),
                getContentDescription(),
                host
        );
    }

    private static ComponentType getComponentType(PAGCMTypeFactory typeFactory, InterfaceType[] functionalItfTypes,
                                                  String hierarchy)
            throws org.objectweb.fractal.api.factory.InstantiationException {

        ArrayList<InterfaceType> itfs = new ArrayList<>();

        // Servers
        itfs.add(typeFactory.createGCMItfType(
                MetricStore.ITF_NAME, MetricStore.class.getName(), false, false, "singleton"
        ));
        itfs.add(typeFactory.createGCMItfType(
                RemoteMonitoringManager.ITF_NAME, RemoteMonitoringManager.class.getName(), false, false, "singleton"
        ));
        itfs.add(typeFactory.createGCMItfType(
                RecordEventListener.ITF_NAME, RecordEventListener.class.getName(), false, false, "singleton"
        ));

        // Clients
        itfs.add(typeFactory.createGCMItfType(
                RecordStore.ITF_NAME, RecordStore.class.getName(), true, false, "singleton"
        ));
        itfs.add(typeFactory.createGCMItfType(
                MetricEventListener.ITF_NAME, MetricEventListener.class.getName(), true, false, "singleton"
        ));

        for (InterfaceType itf : functionalItfTypes) {
            if (itf.isFcClientItf()) {
                String name = itf.getFcItfName() + RemoteMonitoringManager.INTERNAL_MONITORING_SUFFIX;
                if (isSingleton(itf)) {
                    itfs.add(typeFactory.createGCMItfType(name, MonitoringController.class.getName(), true, true, "singleton"));
                } else if (itf instanceof PAGCMInterfaceType) {
                    if (((PAGCMInterfaceType) itf).isGCMGathercastItf()) {
                        itfs.add(typeFactory.createGCMItfType(name, MonitoringController.class.getName(), true, true, "singleton"));
                    } else if (((PAGCMInterfaceType) itf).isGCMMulticastItf()) {
                        itfs.add(typeFactory.createGCMItfType(name, MonitoringController.class.getName(), true, true, "singleton"));
                        // TODO: Singleton??
                    }
                }
            }
        }

        if (hierarchy.equals(Constants.COMPOSITE)) {
            for (InterfaceType itf : functionalItfTypes) {
                if (!itf.isFcClientItf() && isSingleton(itf)) {
                    String name = itf.getFcItfName() + RemoteMonitoringManager.INTERNAL_MONITORING_SUFFIX;
                    itfs.add(typeFactory.createGCMItfType(name, MonitoringController.class.getName(), true, true, "singleton"));
                }
            }
        }

        return typeFactory.createFcType(itfs.toArray(new InterfaceType[itfs.size()]));
    }

    private static ControllerDescription getControllerDescription() {
        return new ControllerDescription(COMPONENT_NAME, Constants.PRIMITIVE, ACSManager.CONTROLLER_CONFIG_PATH);
    }

    private static ContentDescription getContentDescription() {
        return new ContentDescription(MetricStoreImpl.class.getName(), null, new ACSComponentRunActive() {
            @Override
            public void runComponentActivity(Body body) {
                body.setImmediateService("getValue", false);
                (new ComponentMultiActiveService(body)).multiActiveServing();
            }
        }, null);
    }

    private static boolean isSingleton(InterfaceType itf) {
        boolean isSingleton = !itf.isFcCollectionItf();
        if (itf instanceof PAGCMInterfaceType) {
            isSingleton = isSingleton && ((PAGCMInterfaceType) itf).isGCMSingletonItf();
        }
        return isSingleton;
    }
}
