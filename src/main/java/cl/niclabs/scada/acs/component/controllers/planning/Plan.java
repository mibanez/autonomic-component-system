package cl.niclabs.scada.acs.component.controllers.planning;

import cl.niclabs.scada.acs.component.controllers.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Plan
 *
 */
public abstract class Plan implements Serializable {

    private ACSAlarm globalAlarmSubscription = null;
    private final HashMap<String, ACSAlarm> subscriptions = new HashMap<>();

    abstract void doPlanFor(String ruleName, ACSAlarm alarm, MonitoringController monitorController);

    public void globalSubscription(ACSAlarm alarmLevel) {
        globalAlarmSubscription = alarmLevel;
    }

    public void subscribeTo(String ruleName, ACSAlarm alarmLevel) {
        subscriptions.put(ruleName, alarmLevel);
    }

    public void unsubscribeFrom(String ruleName) {
        subscriptions.remove(ruleName);
    }

    public boolean isSubscribedTo(String ruleName, ACSAlarm alarmLevel) {
        if (subscriptions.containsKey(ruleName)) {
            return subscriptions.get(ruleName).getLevel() <= alarmLevel.getLevel();
        } else if (globalAlarmSubscription != null) {
            return globalAlarmSubscription.getLevel() <= alarmLevel.getLevel();
        }
        return false;
    }


}
