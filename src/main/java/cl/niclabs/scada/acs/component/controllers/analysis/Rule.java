package cl.niclabs.scada.acs.component.controllers.analysis;

import cl.niclabs.scada.acs.component.controllers.MonitorController;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Rule
 *
 */
public abstract class Rule implements Serializable {

    private final HashSet<String> subscriptions = new HashSet<>();

    public abstract ACSAlarm verify(MonitorController monitorController);

    public void subscribeTo(String metricName) {
        subscriptions.add(metricName);
    }

    public void unsubscribeFrom(String metricName) {
        subscriptions.remove(metricName);
    }

    public boolean isSubscribedTo(String metricName) {
        return subscriptions.contains(metricName);
    }

}
