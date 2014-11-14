package cl.niclabs.scada.acs.component.controllers.planning;

import cl.niclabs.scada.acs.component.controllers.GenericElement;
import cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Plan
 *
 */
public abstract class Plan extends GenericElement {

    private final HashMap<String, ACSAlarm> subscriptions = new HashMap<>();
    private ACSAlarm globalAlarmSubscription = null;


    public abstract void doPlanFor(String ruleId, ACSAlarm alarm, MonitoringController monitorController);

    // SUBSCRIPTIONS

    public void globallySubscribe(ACSAlarm alarm) {
        globalAlarmSubscription = alarm;
    }

    public void globallyUnsubscribe() {
        globalAlarmSubscription = null;

    }

    public ACSAlarm getGlobalSubscription() {
        return globalAlarmSubscription;
    }

    public void subscribeTo(String ruleId, ACSAlarm alarm) {
        if (subscriptions.containsKey(ruleId)) {
            if (subscriptions.get(ruleId).getLevel() <= alarm.getLevel()) {
                return;
            }
        }
        subscriptions.put(ruleId, alarm);
    }

    public void unsubscribeFrom(String ruleId) {
        subscriptions.remove(ruleId);
    }

    public HashSet<PlanningController.AlarmSubscription> getSubscriptions() {
        HashSet<PlanningController.AlarmSubscription> set = new HashSet<>();
        for (Map.Entry<String, ACSAlarm> entry : subscriptions.entrySet()) {
            set.add(new PlanningController.AlarmSubscription(entry.getKey(), entry.getValue()));
        }
        return set;
    }

    public boolean isSubscribedTo(String ruleId, ACSAlarm alarmLevel) {
        if (subscriptions.containsKey(ruleId)) {
            return subscriptions.get(ruleId).getLevel() <= alarmLevel.getLevel();
        } else if (globalAlarmSubscription != null) {
            return globalAlarmSubscription.getLevel() <= alarmLevel.getLevel();
        }
        return false;
    }

}
