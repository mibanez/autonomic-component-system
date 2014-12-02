package tesis.monitoring.metrics;

import cl.niclabs.scada.acs.component.controllers.monitoring.events.RecordEvent;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.Metric;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.MetricStore;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.RecordStore;

public class StoredPasswordCounter extends Metric<Long> {

    public static final String NAME = "stored-password-counter";

    private long value = 0;
    long time = 0;
    public StoredPasswordCounter() {
        setEnabled(true);
        subscribeTo(RecordEvent.REQUEST_SERVICE_ENDED);
    }

    @Override
    public Long getValue() {
        return value;
    }

    @Override
    public Long calculate(RecordStore recordStore, MetricStore metricStore) {
        value = recordStore.countIncoming();
        return value;
    }
}
