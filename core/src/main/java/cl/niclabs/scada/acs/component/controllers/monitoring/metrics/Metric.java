package cl.niclabs.scada.acs.component.controllers.monitoring.metrics;

import cl.niclabs.scada.acs.component.controllers.GenericElement;
import cl.niclabs.scada.acs.component.controllers.monitoring.events.RecordEvent;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.RecordStore;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * The ACS Metric, it measures the system!
 *
 */
public abstract class Metric<TYPE extends Serializable> extends GenericElement {

    private final Set<RecordEvent> subscriptions = new HashSet<>();

    public abstract TYPE getValue();
    public abstract TYPE calculate(RecordStore recordStore, MetricStore metricStore);

    // SUBSCRIPTIONS

    public void subscribeTo(RecordEvent eventType) {
        subscriptions.add(eventType);
    }

    public void unsubscribeFrom(RecordEvent eventType) {
        subscriptions.remove(eventType);
    }

    public boolean isSubscribedTo(RecordEvent eventType) {
        return subscriptions.contains(eventType);
    }

}
