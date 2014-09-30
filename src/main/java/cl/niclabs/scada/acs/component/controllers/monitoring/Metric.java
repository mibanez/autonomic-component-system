package cl.niclabs.scada.acs.component.controllers.monitoring;

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



    public void subscribeTo(ACSEventType eventType) {
        subscriptions.add(eventType);
    }

    public void unsubscribeFrom(ACSEventType eventType) {
        subscriptions.remove(eventType);
    }

    public boolean isSubscribedTo(ACSEventType eventType) {
        return subscriptions.contains(eventType);
    }

    public void enable() {
        enabled = true;
    }

    public void disable() {
        enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Returns the actual value
     * @return  Actual value
     */
    public abstract TYPE getValue();

    /**
     * Called to recalculate the value
     */
    public abstract void measure(RecordStore recordStore);

}
