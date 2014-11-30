package cl.niclabs.scada.acs.component.controllers.monitoring;


import cl.niclabs.scada.acs.component.ACSManager;
import cl.niclabs.scada.acs.component.controllers.monitoring.events.GCMPAEventListener;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.MetricStore;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.RemoteMonitoringManager;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.body.ComponentRunActive;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.multiactivity.component.ComponentMultiActiveService;

import java.util.ArrayList;

public class MonitoringManagerFactory {

    public static final String COMPONENT_NAME = "MonitoringManager";

    public static Component getMonitoringManagerInstance(PAGCMTypeFactory typeF, PAGenericFactory genericF, Node host)
            throws org.objectweb.fractal.api.factory.InstantiationException {

        return genericF.newNfFcInstance(
                getComponentType(typeF),
                getControllerDescription(),
                getContentDescription(),
                host
        );
    }

    private static ComponentType getComponentType(PAGCMTypeFactory tf) throws org.objectweb.fractal.api.factory.InstantiationException {
        ArrayList<InterfaceType> list = new ArrayList<>();
        list.add(tf.createGCMItfType(MonitoringController.ITF_NAME, MonitoringController.class.getName(), false, false, "singleton"));
        list.add(tf.createGCMItfType(MetricStore.ITF_NAME, MetricStore.class.getName(), true, false, "singleton"));
        list.add(tf.createGCMItfType(RemoteMonitoringManager.ITF_NAME, RemoteMonitoringManager.class.getName(), true, false, "singleton"));
        list.add(tf.createGCMItfType(GCMPAEventListener.ITF_NAME, GCMPAEventListener.class.getName(), true, false, "singleton"));
        return tf.createFcType(list.toArray(new InterfaceType[list.size()]));
    }

    private static ControllerDescription getControllerDescription() {
        return new ControllerDescription("MonitoringManager", Constants.PRIMITIVE, ACSManager.CONTROLLER_CONFIG_PATH);
    }

    private static ContentDescription getContentDescription() {
        return new ContentDescription(MonitoringManagerImpl.class.getName(), null, new MonitoringManagerRunActive(), null);
    }

    public static class MonitoringManagerRunActive implements ComponentRunActive {
        @Override
        public void runComponentActivity(Body body) {
            body.setImmediateService("getValue", false);
            (new ComponentMultiActiveService(body)).multiActiveServing();
        }
    }

}
