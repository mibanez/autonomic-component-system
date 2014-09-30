package cl.niclabs.scada.acs.component.controllers.planning;

import cl.niclabs.scada.acs.component.controllers.ExecutionController;
import cl.niclabs.scada.acs.component.controllers.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.PlanningController;
import cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm;
import cl.niclabs.scada.acs.component.controllers.analysis.RuleEvent;
import cl.niclabs.scada.acs.component.controllers.analysis.RuleEventListener;
import cl.niclabs.scada.acs.component.controllers.utils.ValidWrapper;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.component.controllers.utils.WrongWrapper;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.proactive.core.component.componentcontroller.AbstractPAComponentController;

import java.util.HashMap;

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
    public static final String EXECUTION_CONTROLLER_CLIENT_ITF = "execution-controller-client-itf-nf";

    private MonitoringController monitoringController;
    private ExecutionController executionController;
    private final HashMap<String, Plan> plans = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public Wrapper<Boolean> add(String planId, String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (Plan.class.isAssignableFrom(clazz)) {
                return add(planId, (Class<Plan>) clazz);
            }
            throw new ClassCastException("Can't cast " + clazz.getName() + " to " + Plan.class.getName());
        } catch (Exception e) {
            return new WrongWrapper<>("Fail to get plan class from name " + className, e);
        }
    }

    @Override
    public <PLAN extends Plan> Wrapper<Boolean> add(String planId, Class<PLAN> clazz) {
        try {
            if (plans.containsKey(planId)) {
                return new ValidWrapper<>(false, "A plan with id " + planId + " already exists");
            }
            plans.put(planId, clazz.newInstance());
            return new ValidWrapper<>(true);
        } catch (Exception e) {
            return new WrongWrapper<>("Fail to instantiate plan " + clazz.getName(), e);
        }
    }

    @Override
    public Wrapper<Boolean> remove(String planId) {
        if (plans.containsKey(planId)) {
            plans.remove(planId);
            return new ValidWrapper<>(true);
        }
        return new ValidWrapper<>(false, "No plan found with id " + planId);
    }

    @Override
    public void doPlanFor(String planId, ACSAlarm alarm) {
        for (Plan plan : plans.values()) {
            if (plan.isSubscribedTo(planId, alarm)) {
                plan.doPlanFor(planId, alarm, monitoringController);
            }
        }
    }

    @Override
    public Wrapper<String[]> getRegisteredIds() {
        return new ValidWrapper<>(plans.keySet().toArray(new String[plans.size()]));
    }

    @Override
    public void notifyAlarm(RuleEvent event) {
        doPlanFor(event.getRuleName(), event.getAlarm());
    }

    @Override
    public String[] listFc() {
        return new String[] { MONITORING_CONTROLLER_CLIENT_ITF, EXECUTION_CONTROLLER_CLIENT_ITF };
    }

    @Override
    public Object lookupFc(String name) throws NoSuchInterfaceException {
        switch (name) {
            case MONITORING_CONTROLLER_CLIENT_ITF: return monitoringController;
            case EXECUTION_CONTROLLER_CLIENT_ITF: return executionController;
            default: throw new NoSuchInterfaceException(name);
        }
    }

    @Override
    public void bindFc(String name, Object o) throws NoSuchInterfaceException {
        switch (name) {
            case MONITORING_CONTROLLER_CLIENT_ITF: monitoringController = (MonitoringController) o; break;
            case EXECUTION_CONTROLLER_CLIENT_ITF: executionController = (ExecutionController) o; break;
            default: throw new NoSuchInterfaceException(name);
        }
    }

    @Override
    public void unbindFc(String name) throws NoSuchInterfaceException {
        switch (name) {
            case MONITORING_CONTROLLER_CLIENT_ITF: monitoringController = null; break;
            case EXECUTION_CONTROLLER_CLIENT_ITF: executionController = null; break;
            default: throw new NoSuchInterfaceException(name);
        }
    }

}
