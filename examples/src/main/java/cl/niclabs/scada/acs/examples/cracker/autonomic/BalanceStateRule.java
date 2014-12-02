package cl.niclabs.scada.acs.examples.cracker.autonomic;

import cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm;
import cl.niclabs.scada.acs.component.controllers.analysis.Rule;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.examples.cracker.CrackerConfig;

public class BalanceStateRule extends Rule {

    public static final String NAME = "balanceState";

    private ACSAlarm alarm = ACSAlarm.OK;
    private DistributionPoint lastPoint = new DistributionPoint();

    public BalanceStateRule() {
        subscribeTo(DistributionPointMetric.NAME);
    }

    @Override
    public ACSAlarm getAlarm(MonitoringController monitoringController) {
        return alarm;
    }

    @Override
    public ACSAlarm verify(MonitoringController monitoringController) {

        Wrapper<DistributionPoint> result = monitoringController.getValue(DistributionPointMetric.NAME);

        if (result.isValid()) {
            DistributionPoint point = result.unwrap();
            double dx = point.getX()-lastPoint.getX();
            double dy = point.getY() - lastPoint.getY();
            double d = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));

            if (d > CrackerConfig.D_POINT_MIN_DISTANCE) {
                alarm = ACSAlarm.VIOLATION;
                lastPoint = point;
                //System.out.println("[RULES][DISTRIBUTION] " + point.toString() + " (ALARM! d = " + d + ")");
                return alarm;
            }
            //System.out.println("[RULES][DISTRIBUTION] " + point.toString() + " (OK d = " + d + ")");
        } else {
            System.err.println("[RULES][DISTRIBUTION][WARNING] wrong metric value: " + result.getMessage());
        }

        alarm = ACSAlarm.OK;
        return alarm;
    }
}
