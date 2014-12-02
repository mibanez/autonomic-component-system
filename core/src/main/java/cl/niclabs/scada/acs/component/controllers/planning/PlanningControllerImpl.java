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
import org.apache.log4j.Logger;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.proactive.core.component.componentcontroller.AbstractPAComponentController;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Planning Controller implementation
 *
 */
public class PlanningControllerImpl extends AbstractPAComponentController
        implements PlanningController, RuleEventListener, BindingController {

    private static final Logger logger = ProActiveLogger.getLogger("ACS");

    private final HashMap<String, Plan> plans = new HashMap<>();

    private MonitoringController monitoringCtrl;
    private ExecutionController executionCtrl;


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
            plan.doPlanFor(ruleId, alarm, monitoringCtrl, executionCtrl);
            return new ValidWrapper<>(true);

        }
        return new ValidWrapper<>(false, "No plan registered with id " + id);
    }

    // SUBSCRIPTION


    @Override
    public Wrapper<Boolean> subscribeTo(String id, String ruleId) {
        if (plans.containsKey(id)) {
            plans.get(id).subscribeTo(ruleId);
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
    public Wrapper<HashSet<String>> getSubscriptions(String id) {
        if (plans.containsKey(id)) {
            return new ValidWrapper<>(plans.get(id).getSubscriptions());
        }
        return new WrongWrapper<>("No plan registered with id " + id);
    }

    @Override
    public Wrapper<Boolean> isSubscribedTo(String id, String ruleId) {
        if (plans.containsKey(id)) {
            return new ValidWrapper<>(plans.get(id).isSubscribedTo(ruleId));
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
            if (entry.getValue().isEnabled() && entry.getValue().isSubscribedTo(event.getRuleId())) {
                entry.getValue().doPlanFor(event.getRuleId(), event.getAlarm(), monitoringCtrl, executionCtrl);
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
            case MonitoringController.ITF_NAME: return monitoringCtrl;
            case ExecutionController.ITF_NAME: return executionCtrl;
            default: throw new NoSuchInterfaceException(name);
        }
    }

    @Override
    public void bindFc(String name, Object o) throws NoSuchInterfaceException {
        switch (name) {
            case MonitoringController.ITF_NAME: monitoringCtrl = (MonitoringController) o; break;
            case ExecutionController.ITF_NAME: executionCtrl = (ExecutionController) o; break;
            default: throw new NoSuchInterfaceException(name);
        }
    }

    @Override
    public void unbindFc(String name) throws NoSuchInterfaceException {
        switch (name) {
            case MonitoringController.ITF_NAME: monitoringCtrl = null; break;
            case ExecutionController.ITF_NAME: executionCtrl = null; break;
            default: throw new NoSuchInterfaceException(name);
        }
    }

}
