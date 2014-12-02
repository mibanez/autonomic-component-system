package cl.niclabs.scada.acs.examples.cracker.autonomic;

import cl.niclabs.scada.acs.component.controllers.monitoring.events.RecordEvent;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.Metric;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.MetricStore;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.OutgoingRecord;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.RCondition;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.RecordStore;

import java.util.List;


public class Solver3AvgRespTimeMetric extends AvgRespTimeMetric {

    protected void printMessage() {
        System.out.println("[METRICS][SOLVER2_AVG_RESPONSE_TIME] value = " + getValue());
    }
}
