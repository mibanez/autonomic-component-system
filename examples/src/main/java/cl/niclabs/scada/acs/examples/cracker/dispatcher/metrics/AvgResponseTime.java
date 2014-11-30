package cl.niclabs.scada.acs.examples.cracker.dispatcher.metrics;

import cl.niclabs.scada.acs.component.controllers.monitoring.events.RecordEvent;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.Metric;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.MetricStore;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.IncomingRecord;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.RCondition;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.RecordStore;

import java.util.List;


public class AvgResponseTime extends Metric<Long> {

    public static final String NAME = "avg-response-time";
    private long value = -1;

    public AvgResponseTime() {
        subscribeTo(RecordEvent.REQUEST_SERVICE_ENDED);
    }

    @Override
    public Long getValue() {
        return value;
    }

    @Override
    public Long calculate(RecordStore recordStore, MetricStore metricStore) {

        List<IncomingRecord> records = recordStore.getIncoming(new RCondition<IncomingRecord>() {
            @Override
            public boolean evaluate(IncomingRecord record) {
                return record.isFinished();
            }
        }, 3);

        long aux = 0;
        for (IncomingRecord r : records) {
            aux += r.getServiceEndedTime() - r.getReceptionTime();
        }

        value = aux/records.size();
        System.out.println("[METRICS][AVG_RESPONSE_TIME] value = " + value);
        return value;
    }
}
