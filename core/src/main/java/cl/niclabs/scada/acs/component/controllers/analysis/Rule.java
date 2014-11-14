package cl.niclabs.scada.acs.component.controllers.analysis;

import cl.niclabs.scada.acs.component.controllers.GenericElement;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;

import java.util.HashSet;

/**
 * Rule
 *
 */
public abstract class Rule extends GenericElement {

    private final HashSet<String> subscriptions = new HashSet<>();


    public abstract ACSAlarm getAlarm(MonitoringController monitoringController);

    public abstract ACSAlarm verify(MonitoringController monitoringController);

    // SUBSCRIPTION

    public void subscribeTo(String metricName) {
        subscriptions.add(metricName);
    }

    public void unsubscribeFrom(String metricName) {
        subscriptions.remove(metricName);
    }

    public boolean isSubscribedTo(String metricName) {
        return subscriptions.contains(metricName);
    }

    public HashSet<String> getSubscriptions() {
        return new HashSet<>(subscriptions);
    }

}
