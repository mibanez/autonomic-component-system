package cl.niclabs.scada.acs.examples.cracker.common.metrics;

import cl.niclabs.scada.acs.component.controllers.monitoring.events.RecordEvent;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.Metric;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.MetricStore;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.IncomingRecord;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.OutgoingRecord;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.RCondition;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.RecordStore;

import java.util.List;


public class AvgResponseTime extends Metric<Long> {

    public static final String NAME = "avg-response-time";
    private long value = 0;

    public AvgResponseTime() {
        subscribeTo(RecordEvent.RESPONSE_RECEIVED);
        setEnabled(true);
    }

    @Override
    public Long getValue() {
        return value;
    }

    @Override
    public Long calculate(RecordStore recordStore, MetricStore metricStore) {

        List<OutgoingRecord> records = recordStore.fromOutgoing(new RCondition<OutgoingRecord>() {
            @Override
            public boolean evaluate(OutgoingRecord record) {
                return record.isFinished();
            }
        }, 5);

        long aux = 0;
        for (OutgoingRecord record : records) {
            aux += record.getResponseReceivedTime() - record.getSentTime();
        }


        value = records.size() == 0 ? 0 : aux/records.size();
        printMessage();
        return value;
    }

    protected void printMessage() {
        System.out.println("[METRICS][AVG_RESPONSE_TIME] value = " + value);
    }
}
