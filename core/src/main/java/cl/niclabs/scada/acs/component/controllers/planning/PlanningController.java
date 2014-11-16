package cl.niclabs.scada.acs.component.controllers.planning;

import cl.niclabs.scada.acs.component.controllers.DuplicatedElementIdException;
import cl.niclabs.scada.acs.component.controllers.ElementNotFoundException;
import cl.niclabs.scada.acs.component.controllers.InvalidElementException;
import cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Planning controller
 *
 */
public interface PlanningController {

    public static class AlarmSubscription implements Serializable {
        private final String ruleId;
        private final ACSAlarm alarm;
        AlarmSubscription(String ruleId, ACSAlarm alarm) {
            this.ruleId = ruleId;
            this.alarm = alarm;
        }
        public String getRuleId() {
            return ruleId;
        }
        public ACSAlarm getAlarm() {
            return alarm;
        }
    }

    public void add(String id, String className) throws DuplicatedElementIdException, InvalidElementException;

    public <PLAN extends Plan> void add(String id, Class<PLAN> clazz) throws DuplicatedElementIdException, InvalidElementException;

    public void remove(String id) throws ElementNotFoundException;

    public HashSet<String> getRegisteredIds();

    // PLANNING

    Wrapper<Boolean> doPlanFor(String id, String ruleId, ACSAlarm alarm);

    // SUBSCRIPTIONS

    public Wrapper<Boolean> globallySubscribe(String id, ACSAlarm alarmLevel);

    public Wrapper<Boolean> globallyUnsubscribe(String id);

    public Wrapper<ACSAlarm> getGlobalSubscription(String id);

    public Wrapper<Boolean> subscribeTo(String id, String ruleId, ACSAlarm alarmLevel);

    public Wrapper<Boolean> unsubscribeFrom(String id, String ruleId);

    public Wrapper<HashSet<AlarmSubscription>> getSubscriptions(String id);

    public Wrapper<Boolean> isSubscribedTo(String id, String ruleId, ACSAlarm alarmLevel);

    // STATE

    public Wrapper<Boolean> isEnabled(String id);

    public Wrapper<Boolean> setEnabled(String id, boolean enabled);
}
