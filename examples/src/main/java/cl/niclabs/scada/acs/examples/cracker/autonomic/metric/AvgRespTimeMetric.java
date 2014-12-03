package cl.niclabs.scada.acs.examples.cracker.autonomic.metric;

import cl.niclabs.scada.acs.component.controllers.monitoring.events.RecordEvent;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.Metric;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.MetricStore;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.IncomingRecord;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.OutgoingRecord;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.RCondition;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.RecordStore;
import cl.niclabs.scada.acs.examples.cracker.CrackerConfig;

import java.util.List;


public class AvgRespTimeMetric extends Metric<Long> {

    public static final String NAME = "avgRespTime";

    private long avgTime;
    private int nOfRecords;
    private RCondition<IncomingRecord> finishedCondition;


    public AvgRespTimeMetric() {
        this.avgTime = 0;
        this.nOfRecords = CrackerConfig.RECORD_SAMPLES_SIZE;
        this.finishedCondition = new RCondition<IncomingRecord>() {
            @Override
            public boolean evaluate(IncomingRecord record) {
                return record.isFinished();
            }
        };

        setEnabled(true);
        subscribeTo(RecordEvent.REQUEST_SERVICE_ENDED);
    }

    @Override
    public Long getValue() {
        return avgTime;
    }

    @Override
    public Long calculate(RecordStore recordStore, MetricStore metricStore) {

        List<IncomingRecord> records = recordStore.getIncoming(finishedCondition, nOfRecords);

        long sumOfTimes = 0;
        for (IncomingRecord record : records) {
            sumOfTimes += record.getServiceEndedTime() - record.getReceptionTime();
        }

        avgTime = records.size() == 0 ? 0 : sumOfTimes/records.size();

        return avgTime;
    }
}
