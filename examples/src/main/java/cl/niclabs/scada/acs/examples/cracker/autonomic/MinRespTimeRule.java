package cl.niclabs.scada.acs.examples.cracker.autonomic;

import cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm;
import cl.niclabs.scada.acs.component.controllers.analysis.Rule;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;
import cl.niclabs.scada.acs.examples.cracker.CrackerConfig;


public class MinRespTimeRule extends Rule {

    public static final String NAME = "minRespTime";

    private ACSAlarm alarm = ACSAlarm.OK;

    public MinRespTimeRule() {
        subscribeTo(AvgRespTimeMetric.NAME);
    }

    @Override
    public ACSAlarm getAlarm(MonitoringController monitoringController) {
        return alarm;
    }

    @Override
    public ACSAlarm verify(MonitoringController monitoringController) {
        if ((long) monitoringController.getValue(AvgRespTimeMetric.NAME).unwrap() < CrackerConfig.MIN_RESPONSE_TIME) {
            alarm = ACSAlarm.VIOLATION;
        } else {
            alarm = ACSAlarm.OK;
        }
        return alarm;
    }
}
