package cl.niclabs.scada.acs.component.controllers.monitoring.records;

import cl.niclabs.scada.acs.component.ACSManager;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.InstantiationException;
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

public class RecordStoreFactory {

    public static final String COMPONENT_NAME = "RecordStore";

    public static Component getRecordStoreInstance(PAGCMTypeFactory typeF, PAGenericFactory genericF, Node host)
            throws InstantiationException {

        return genericF.newNfFcInstance(
                getRecordStoreType(typeF),
                getControllerDescription(),
                getContentDescription(),
                host
        );
    }

    private static ComponentType getRecordStoreType(PAGCMTypeFactory typeFactory) throws InstantiationException {
        ArrayList<InterfaceType> itfs = new ArrayList<>();
        itfs.add(typeFactory.createGCMItfType(
                RecordStore.ITF_NAME, RecordStore.class.getName(), false, false, "singleton"
        ));
        return typeFactory.createFcType(itfs.toArray(new InterfaceType[itfs.size()]));
    }

    private static ControllerDescription getControllerDescription() {
        return new ControllerDescription(COMPONENT_NAME, Constants.PRIMITIVE, ACSManager.CONTROLLER_CONFIG_PATH);
    }

    private static ContentDescription getContentDescription() {
        return new ContentDescription(RecordStoreImpl.class.getName(), null, new RecordStoreRunActive(), null);
    }

    public static class RecordStoreRunActive implements ComponentRunActive {
        @Override
        public void runComponentActivity(Body body) {
            (new ComponentMultiActiveService(body)).multiActiveServing();
        }
    }
}
