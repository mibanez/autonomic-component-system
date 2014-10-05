package cl.niclabs.scada.acs.component.controllers;

import cl.niclabs.scada.acs.component.controllers.monitoring.ACSEventType;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.RecordStore;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * The ACS Metric, it measures the system!
 *
 */
public abstract class Metric<TYPE extends Serializable> implements Serializable {

    private final Set<ACSEventType> subscriptions = new HashSet<>();
    private boolean enabled = false;

    public void subscribeTo(ACSEventType eventType) throws CommunicationException {
        subscriptions.add(eventType);
    }

    public void unsubscribeFrom(ACSEventType eventType) throws CommunicationException {
        subscriptions.remove(eventType);
    }

    public boolean isSubscribedTo(ACSEventType eventType) throws CommunicationException {
        return subscriptions.contains(eventType);
    }

    public void setEnabled(boolean enabled) throws CommunicationException {
        this.enabled = enabled;
    }

    public boolean isEnabled() throws CommunicationException {
        return enabled;
    }

    /**
     * Returns the actual value
     * @return  Actual value
     */
    public abstract TYPE getValue() throws CommunicationException;

    /**
     * Called to recalculate the value
     */
    public abstract TYPE measure(RecordStore recordStore) throws CommunicationException;

}
