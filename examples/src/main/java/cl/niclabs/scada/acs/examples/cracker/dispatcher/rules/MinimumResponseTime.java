package cl.niclabs.scada.acs.examples.cracker.dispatcher.rules;

import cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm;
import cl.niclabs.scada.acs.component.controllers.analysis.Rule;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;
import cl.niclabs.scada.acs.examples.cracker.common.metrics.AvgResponseTime;


public class MinimumResponseTime extends Rule {

    public static final String NAME = "minimum-response-time";

    private ACSAlarm alarm = ACSAlarm.OK;

    public MinimumResponseTime() {
        subscribeTo(AvgResponseTime.NAME);
    }

    @Override
    public ACSAlarm getAlarm(MonitoringController monitoringController) {
        return alarm;
    }

    @Override
    public ACSAlarm verify(MonitoringController monitoringController) {
        if ((long) monitoringController.getValue(AvgResponseTime.NAME).unwrap() < 10000) {
            alarm = ACSAlarm.VIOLATION;
            System.out.println("[RULES][MINIMUM_RESPONSE_TIME] " + alarm);
        }
        return alarm;
    }
}
