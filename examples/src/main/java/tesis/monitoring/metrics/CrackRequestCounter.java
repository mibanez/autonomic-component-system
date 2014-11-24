package tesis.monitoring.metrics;

import cl.niclabs.scada.acs.component.controllers.monitoring.events.RecordEvent;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.Metric;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.MetricStore;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.RecordStore;

public class CrackRequestCounter extends Metric<Long> {

    public static final String NAME = "crack-request-counter";
    private long counter = 0;

    public CrackRequestCounter() {
        subscribeTo(RecordEvent.REQUEST_RECEIVED);
    }

    @Override
    public Long getValue() {
        return counter;
    }

    @Override
    public Long calculate(RecordStore recordStore, MetricStore metricStore) {
        counter = recordStore.fromIncoming().getRecords().size();
        return counter;
    }
}
