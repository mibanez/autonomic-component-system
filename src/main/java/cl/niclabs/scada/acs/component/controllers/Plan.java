package cl.niclabs.scada.acs.component.controllers;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Plan
 *
 */
public abstract class Plan implements Serializable {

    private final HashMap<String, ACSAlarm> subscriptions = new HashMap<>();
    private ACSAlarm globalAlarmSubscription = null;

    public abstract void doPlanFor(String ruleId, ACSAlarm alarm, MonitoringController monitorController) throws CommunicationException;

    public void globalSubscription(ACSAlarm alarmLevel) throws CommunicationException {
        globalAlarmSubscription = alarmLevel;
    }

    public void subscribeTo(String ruleId, ACSAlarm alarmLevel) throws CommunicationException {
        subscriptions.put(ruleId, alarmLevel);
    }

    public void unsubscribeFrom(String ruleId) throws CommunicationException {
        subscriptions.remove(ruleId);
    }

    public boolean isSubscribedTo(String ruleId, ACSAlarm alarmLevel) throws CommunicationException {
        if (subscriptions.containsKey(ruleId)) {
            return subscriptions.get(ruleId).getLevel() <= alarmLevel.getLevel();
        } else if (globalAlarmSubscription != null) {
            return globalAlarmSubscription.getLevel() <= alarmLevel.getLevel();
        }
        return false;
    }

}
