package cl.niclabs.scada.acs.component.controllers.execution;

import cl.niclabs.scada.acs.component.ACSManager;
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


public class ExecutionControllerFactory {

    public static final String COMPONENT_NAME = "ExecutionController";

    public static Component getExecutionControllerInstance(PAGCMTypeFactory typeF, PAGenericFactory genericF, Node host)
            throws org.objectweb.fractal.api.factory.InstantiationException {

        return genericF.newNfFcInstance(
                getComponentType(typeF),
                getControllerDescription(),
                getContentDescription(),
                host
        );
    }

    private static ComponentType getComponentType(PAGCMTypeFactory typeFactory) throws org.objectweb.fractal.api.factory.InstantiationException {
        ArrayList<InterfaceType> itfTypes = new ArrayList<>();
        itfTypes.add(typeFactory.createGCMItfType(ExecutionController.ITF_NAME, ExecutionController.class.getName(), false, false, "singleton"));
        return typeFactory.createFcType(itfTypes.toArray(new InterfaceType[itfTypes.size()]));
    }

    private static ControllerDescription getControllerDescription() {
        return new ControllerDescription(COMPONENT_NAME, Constants.PRIMITIVE, ACSManager.CONTROLLER_CONFIG_PATH);
    }

    private static ContentDescription getContentDescription() {
        return new ContentDescription(ExecutionControllerImpl.class.getName(), null, new ExecutionControllerRunActive(), null);
    }

    public static class ExecutionControllerRunActive implements ComponentRunActive {
        @Override
        public void runComponentActivity(Body body) {
            (new ComponentMultiActiveService(body)).multiActiveServing();
        }
    }
}
