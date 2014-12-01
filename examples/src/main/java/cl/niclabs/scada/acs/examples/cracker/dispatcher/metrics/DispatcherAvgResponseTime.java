package cl.niclabs.scada.acs.examples.cracker.dispatcher.metrics;

import cl.niclabs.scada.acs.component.controllers.monitoring.events.RecordEvent;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.Metric;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.MetricStore;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.IncomingRecord;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.RCondition;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.RecordStore;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.examples.cracker.common.components.Cracker;
import cl.niclabs.scada.acs.examples.cracker.common.components.Solver;
import cl.niclabs.scada.acs.examples.cracker.common.metrics.AvgResponseTime;

import java.io.Serializable;
import java.util.List;


public class DispatcherAvgResponseTime extends AvgResponseTime {

    public static final String NAME = "dispatcher-avg-response-time";

    @Override
    protected void printMessage() {
        System.out.println("[METRICS][DISPATCHER_AVG_RESPONSE_TIME] value = " + getValue());
    }
}
