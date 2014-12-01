package cl.niclabs.scada.acs.component.controllers.monitoring.events;

import cl.niclabs.scada.acs.component.ACSManager;
import cl.niclabs.scada.acs.component.body.ACSComponentRunActive;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.RecordStore;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.multiactivity.component.ComponentMultiActiveService;

import java.util.ArrayList;


public class GCMPAEventListenerFactory {

    public static final String COMPONENT_NAME = "GCMPAEventListener";

    public static Component getGCMPAEventListenerInstance(PAGCMTypeFactory typeF, PAGenericFactory genericF, Node host)
            throws org.objectweb.fractal.api.factory.InstantiationException {

        return genericF.newNfFcInstance(
                getComponentType(typeF),
                getControllerDescription(),
                getContentDescription(),
                host
        );
    }

    private static ComponentType getComponentType(PAGCMTypeFactory typeFactory) throws InstantiationException {
        ArrayList<InterfaceType> itfs = new ArrayList<>();
        itfs.add(typeFactory.createGCMItfType(
                GCMPAEventListener.ITF_NAME, GCMPAEventListener.class.getName(), false, false, "singleton"
        ));
        itfs.add(typeFactory.createGCMItfType(
                RecordEventListener.ITF_NAME, RecordEventListener.class.getName(), true, false, "singleton"
        ));
        itfs.add(typeFactory.createGCMItfType(
                RecordStore.ITF_NAME, RecordStore.class.getName(), true, false, "singleton"
        ));
        return typeFactory.createFcType(itfs.toArray(new InterfaceType[itfs.size()]));
    }

    private static ControllerDescription getControllerDescription() {
        return new ControllerDescription(COMPONENT_NAME, Constants.PRIMITIVE, ACSManager.CONTROLLER_CONFIG_PATH);
    }

    private static ContentDescription getContentDescription() {
        return new ContentDescription(GCMPAEventListenerImpl.class.getName(), null, new ACSComponentRunActive() {
            @Override
            public void runComponentActivity(Body body) {
                (new ComponentMultiActiveService(body)).multiActiveServing();
            }
        }, null);
    }
}
