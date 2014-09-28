package cl.niclabs.scada.acs.component.controllers.planning;

import cl.niclabs.scada.acs.component.controllers.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.PlanningController;
import cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm;
import cl.niclabs.scada.acs.component.controllers.analysis.RuleEvent;
import cl.niclabs.scada.acs.component.controllers.analysis.RuleEventListener;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
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

    private MonitoringController monitoringController;
    private final HashMap<String, Plan> plans = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public Wrapper<Boolean> addPlan(String name, String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (Plan.class.isAssignableFrom(clazz)) {
                return addPlan(name, (Class<Plan>) clazz);
            }
            return new Wrapper<>(false, "Plan class is not assignable from the found class " + clazz.getName());
        }
        catch (Exception e) {
            return new Wrapper<>(false, "Fail to get plan class " + className, e);
        }
    }

    @Override
    public <PLAN extends Plan> Wrapper<Boolean> addPlan(String name, Class<PLAN> clazz) {
        try {
            if (plans.containsKey(name)) {
                return new Wrapper<>(false, "The plan name " + name + " already exists");
            }
            plans.put(name, clazz.newInstance());
            return new Wrapper<>(true, "Plan " + name + " added correctly");
        }
        catch (Exception e) {
            return new Wrapper<>(false, "Fail to instantiate plan " + clazz.getName(), e);
        }
    }

    @Override
    public Wrapper<Boolean> removePlan(String planName) {
        if (plans.containsKey(planName)) {
            plans.remove(planName);
            return new Wrapper<>(true);
        }
        return new Wrapper<>(false, String.format("no metric with name %s found", planName));
    }

    @Override
    public void doPlanFor(String ruleName, ACSAlarm alarm) {
        for (Plan plan : plans.values()) {
            if (plan.isSubscribedTo(ruleName, alarm)) {
                plan.doPlanFor(ruleName, alarm, monitoringController);
            }
        }
    }

    @Override
    public Wrapper<String[]> getPlanNames() {
        return new Wrapper<>(plans.keySet().toArray(new String[plans.size()]));
    }

    @Override
    public void notifyAlarm(RuleEvent event) {
        doPlanFor(event.getRuleName(), event.getAlarm());
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
