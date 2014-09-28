package cl.niclabs.scada.acs.component.controllers.planning;

import cl.niclabs.scada.acs.component.controllers.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.PlanningController;
import cl.niclabs.scada.acs.component.controllers.analysis.RuleEvent;
import cl.niclabs.scada.acs.component.controllers.analysis.RuleEventListener;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.proactive.core.component.componentcontroller.AbstractPAComponentController;

/**
 * Planning Controller implementation
 *
 */
public class PlanningControllerImpl extends AbstractPAComponentController
        implements PlanningController, RuleEventListener, BindingController {

    public static final String CONTROLLER_NAME = "PlanningController";
    public static final String PLANNING_CONTROLLER_SERVER_ITF = "planning-controller-server-itf-nf";
    public static final String RULE_EVENT_LISTENER_SERVER_ITF = "rule-event-listener-server-itf-nf";
    public static final String MONITORING_CONTROLLER_CLIENT_ITF = "monitor-controller-client-itf-nf";

    private MonitoringController monitoringController;

    @Override
    public void notifyAlarm(RuleEvent event) {

    }

    @Override
    public String[] listFc() {
        return new String[] { MONITORING_CONTROLLER_CLIENT_ITF };
    }

    @Override
    public Object lookupFc(String name) throws NoSuchInterfaceException {
        switch (name) {
            case MONITORING_CONTROLLER_CLIENT_ITF: return monitoringController;
            default: throw new NoSuchInterfaceException(name);
        }
    }

    @Override
    public void bindFc(String name, Object o) throws NoSuchInterfaceException {
        switch (name) {
            case MONITORING_CONTROLLER_CLIENT_ITF: monitoringController = (MonitoringController) o; break;
            default: throw new NoSuchInterfaceException(name);
        }
    }

    @Override
    public void unbindFc(String name) throws NoSuchInterfaceException {
        switch (name) {
            case MONITORING_CONTROLLER_CLIENT_ITF: monitoringController = null; break;
            default: throw new NoSuchInterfaceException(name);
        }
    }

}
