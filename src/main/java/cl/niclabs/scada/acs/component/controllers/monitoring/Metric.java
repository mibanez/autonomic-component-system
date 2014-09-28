package cl.niclabs.scada.acs.component.controllers.monitoring;

import cl.niclabs.scada.acs.component.controllers.monitoring.records.RecordStore;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * The ACS Metric, it measures the system!
 *
 */
public abstract class Metric<TYPE extends Serializable> implements Serializable {

    private final Set<ACSEventType> subscriptions = new HashSet<>();

    /**
     * Called to recalculate the value
     */
    public abstract void measure(RecordStore recordStore);

    /**
     * Returns the actual value
     * @return  Actual value
     */
    public abstract TYPE getValue();

    /**
     * Returns the actual value, wrapped
     * @return  Wrapped actual value
     */
    public Wrapper<TYPE> getWrappedValue() {
        return new Wrapper<>(getValue());
    }

    public void subscribeTo(ACSEventType eventType) {
        subscriptions.add(eventType);
    }

    public void unsubscribeFrom(ACSEventType eventType) {
        subscriptions.remove(eventType);
    }

    public boolean isSubscribedTo(ACSEventType eventType) {
        return subscriptions.contains(eventType);
    }

}
