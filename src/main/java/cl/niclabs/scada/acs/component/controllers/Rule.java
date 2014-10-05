package cl.niclabs.scada.acs.component.controllers;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Rule
 *
 */
public abstract class Rule implements Serializable {

    private final HashSet<String> subscriptions = new HashSet<>();

    public void subscribeTo(String metricName) throws CommunicationException {
        subscriptions.add(metricName);
    }

    public void unsubscribeFrom(String metricName) throws CommunicationException {
        subscriptions.remove(metricName);
    }

    public boolean isSubscribedTo(String metricName) throws CommunicationException {
        return subscriptions.contains(metricName);
    }

    public abstract ACSAlarm verify(MonitoringController monitoringController) throws CommunicationException;

}
