package cl.niclabs.scada.acs.examples.cracker.autonomic.rule;

import cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm;
import cl.niclabs.scada.acs.component.controllers.analysis.Rule;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.examples.cracker.CrackerConfig;
import cl.niclabs.scada.acs.examples.cracker.autonomic.metric.AvgRespTimeMetric;

import static cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm.*;


public class MinRespTimeRule extends Rule {

    public static final String NAME = "minRespTime";

    private ACSAlarm alarm;
    private long threshold;
    private String avgRespTimeMetricName;

    public MinRespTimeRule() {
        this.alarm = OK;
        this.threshold = CrackerConfig.MIN_RESPONSE_TIME;
        this.avgRespTimeMetricName = AvgRespTimeMetric.NAME;

        subscribeTo(AvgRespTimeMetric.NAME);
    }

    @Override
    public ACSAlarm getAlarm(MonitoringController monitoringController) {
        return alarm;
    }

    @Override
    public ACSAlarm verify(MonitoringController monitoringCtrl) {

        Wrapper<Long> respTimeWrapper = monitoringCtrl.getValue(avgRespTimeMetricName);

        if ( !respTimeWrapper.isValid() ) {
            alarm = ERROR;
        } else if (respTimeWrapper.unwrap() < threshold) {
            alarm = VIOLATION;
        } else {
            alarm = OK;
        }

        return alarm;
    }
}
