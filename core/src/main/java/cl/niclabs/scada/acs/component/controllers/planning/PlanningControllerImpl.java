package cl.niclabs.scada.acs.component.controllers.planning;

import cl.niclabs.scada.acs.component.controllers.DuplicatedElementIdException;
import cl.niclabs.scada.acs.component.controllers.ElementNotFoundException;
import cl.niclabs.scada.acs.component.controllers.InvalidElementException;
import cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm;
import cl.niclabs.scada.acs.component.controllers.analysis.RuleEvent;
import cl.niclabs.scada.acs.component.controllers.analysis.RuleEventListener;
import cl.niclabs.scada.acs.component.controllers.execution.ExecutionController;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.utils.ValidWrapper;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.component.controllers.utils.WrongWrapper;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.proactive.core.component.componentcontroller.AbstractPAComponentController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Planning Controller implementation
 *
 */
public class PlanningControllerImpl extends AbstractPAComponentController
        implements PlanningController, RuleEventListener, BindingController {

    private static final Logger logger = LoggerFactory.getLogger(PlanningController.class);

    private final HashMap<String, Plan> plans = new HashMap<>();

    private MonitoringController monitoringController;
    private ExecutionController executionController;


    @Override
    @SuppressWarnings("unchecked")
    public void add(String planId, String className) throws DuplicatedElementIdException, InvalidElementException {
        try {
            Class<?> clazz = Class.forName(className);
            if (!Plan.class.isAssignableFrom(clazz)) {
                throw new InvalidElementException("Can't cast " + clazz.getName() + " to " + Plan.class.getName());
            }
            add(planId, (Class<Plan>) clazz);
        } catch (ClassNotFoundException e) {
            throw new InvalidElementException(e);
        }
    }

    @Override
    public <PLAN extends Plan> void add(String id, Class<PLAN> clazz)
            throws DuplicatedElementIdException, InvalidElementException {

        if (plans.containsKey(id)) {
            throw new DuplicatedElementIdException(id);
        } else try {
            plans.put(id, clazz.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new InvalidElementException(e);
        }
    }

    @Override
    public void remove(String id) throws ElementNotFoundException {
        if (plans.containsKey(id)) {
            plans.remove(id);
        } else {
            throw new ElementNotFoundException(id);
        }
    }

    @Override
    public HashSet<String> getRegisteredIds() {
        return new HashSet<>(plans.keySet());
    }

    // PLAN

    @Override
    public Wrapper<Boolean> doPlanFor(String id, String ruleId, ACSAlarm alarm) {
        if (plans.containsKey(id)) {
            Plan plan = plans.get(id);
            plan.doPlanFor(ruleId, alarm, monitoringController);
            return new ValidWrapper<>(true);

        }
        return new ValidWrapper<>(false, "No plan registered with id " + id);
    }

    // SUBSCRIPTION

    @Override
    public Wrapper<Boolean> globallySubscribe(String id, ACSAlarm alarm) {
        if (plans.containsKey(id)) {
            plans.get(id).globallySubscribe(alarm);
            return new ValidWrapper<>(true);
        }
        return new ValidWrapper<>(false, "No plan registered with id " + id);
    }

    @Override
    public Wrapper<Boolean> globallyUnsubscribe(String id) {
        if (plans.containsKey(id)) {
            plans.get(id).globallyUnsubscribe();
            return new ValidWrapper<>(true);
        }
        return new ValidWrapper<>(false, "No plan registered with id " + id);
    }

    @Override
    public Wrapper<ACSAlarm> getGlobalSubscription(String id) {
        if (plans.containsKey(id)) {
            new ValidWrapper<>(plans.get(id).getGlobalSubscription());
        }
        return new WrongWrapper<>("No plan registered with id " + id);
    }

    @Override
    public Wrapper<Boolean> subscribeTo(String id, String ruleId, ACSAlarm alarmLevel) {
        if (plans.containsKey(id)) {
            plans.get(id).subscribeTo(ruleId, alarmLevel);
            return new ValidWrapper<>(true);
        }
        return new ValidWrapper<>(false, "No plan registered with id " + id);
    }

    @Override
    public Wrapper<Boolean> unsubscribeFrom(String id, String ruleId) {
        if (plans.containsKey(id)) {
            plans.get(id).unsubscribeFrom(ruleId);
            return new ValidWrapper<>(true);
        }
        return new ValidWrapper<>(false, "No plan registered with id " + id);
    }

    @Override
    public Wrapper<HashSet<AlarmSubscription>> getSubscriptions(String id) {
        if (plans.containsKey(id)) {
            return new ValidWrapper<>(plans.get(id).getSubscriptions());
        }
        return new WrongWrapper<>("No plan registered with id " + id);
    }

    @Override
    public Wrapper<Boolean> isSubscribedTo(String id, String ruleId, ACSAlarm alarmLevel) {
        if (plans.containsKey(id)) {
            return new ValidWrapper<>(plans.get(id).isSubscribedTo(ruleId, alarmLevel));
        }
        return new WrongWrapper<>("No plan registered with id " + id);
    }

    // STATE

    @Override
    public Wrapper<Boolean> isEnabled(String id) {
        if (plans.containsKey(id)) {
            return new ValidWrapper<>(plans.get(id).isEnabled());
        }
        return new WrongWrapper<>("No plan registered with id " + id);
    }

    @Override
    public Wrapper<Boolean> setEnabled(String id, boolean enabled) {
        if (plans.containsKey(id)) {
            plans.get(id).setEnabled(enabled);
            return new ValidWrapper<>(true);
        }
        return new ValidWrapper<>(false, "No plan registered with id " + id);
    }

    // RULE EVENT LISTENER

    @Override
    public void notifyAlarm(RuleEvent event) {
        for (Map.Entry<String, Plan> entry : plans.entrySet()) {
            if (entry.getValue().isSubscribedTo(event.getRuleId(), event.getAlarm())) {
                entry.getValue().doPlanFor(event.getRuleId(), event.getAlarm(), monitoringController);
            }
        }
    }

    // BINDING CONTROLLER

    @Override
    public String[] listFc() {
        return new String[] { MonitoringController.ITF_NAME, ExecutionController.ITF_NAME };
    }

    @Override
    public Object lookupFc(String name) throws NoSuchInterfaceException {
        switch (name) {
            case MonitoringController.ITF_NAME: return monitoringController;
            case ExecutionController.ITF_NAME: return executionController;
            default: throw new NoSuchInterfaceException(name);
        }
    }

    @Override
    public void bindFc(String name, Object o) throws NoSuchInterfaceException {
        switch (name) {
            case MonitoringController.ITF_NAME: monitoringController = (MonitoringController) o; break;
            case ExecutionController.ITF_NAME: executionController = (ExecutionController) o; break;
            default: throw new NoSuchInterfaceException(name);
        }
    }

    @Override
    public void unbindFc(String name) throws NoSuchInterfaceException {
        switch (name) {
            case MonitoringController.ITF_NAME: monitoringController = null; break;
            case ExecutionController.ITF_NAME: executionController = null; break;
            default: throw new NoSuchInterfaceException(name);
        }
    }

}
