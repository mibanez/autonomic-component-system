package cl.niclabs.scada.acs.component.controllers.analysis;


import cl.niclabs.scada.acs.component.ACSUtils;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.MetricEventListener;
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

public class AnalysisControllerFactory {

    public static final String COMPONENT_NAME = "AnalysisController";

    public static Component getAnalysisControllerInstance(PAGCMTypeFactory typeF, PAGenericFactory genericF, Node host)
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
        itfTypes.add(typeFactory.createGCMItfType(AnalysisController.ITF_NAME, AnalysisController.class.getName(), false, false, "singleton"));
        itfTypes.add(typeFactory.createGCMItfType(MetricEventListener.ITF_NAME, MetricEventListener.class.getName(), false, false, "singleton"));
        itfTypes.add(typeFactory.createGCMItfType(MonitoringController.ITF_NAME, MonitoringController.class.getName(), true, false, "singleton"));
        itfTypes.add(typeFactory.createGCMItfType(RuleEventListener.ITF_NAME, RuleEventListener.class.getName(), true, false, "singleton"));

        return typeFactory.createFcType(itfTypes.toArray(new InterfaceType[itfTypes.size()]));
    }

    private static ControllerDescription getControllerDescription() {
        return new ControllerDescription(COMPONENT_NAME, Constants.PRIMITIVE, ACSUtils.CONTROLLER_CONFIG_PATH);
    }

    private static ContentDescription getContentDescription() {
        return new ContentDescription(AnalysisControllerImpl.class.getName(), null, new AnalysisControllerRunActive(), null);
    }

    public static class AnalysisControllerRunActive implements ComponentRunActive {
        @Override
        public void runComponentActivity(Body body) {
            (new ComponentMultiActiveService(body)).multiActiveServing();
        }
    }
}
