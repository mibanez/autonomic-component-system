package cl.niclabs.scada.acs.examples.cracker.autonomic.rule;

import cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm;
import cl.niclabs.scada.acs.component.controllers.analysis.Rule;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.examples.cracker.CrackerConfig;
import cl.niclabs.scada.acs.examples.cracker.autonomic.metric.DistributionPoint;
import cl.niclabs.scada.acs.examples.cracker.autonomic.metric.DistributionPointMetric;

public class BalanceStateRule extends Rule {

    public static final String NAME = "balanceState";

    private DistributionPoint lastPoint;
    private String dstPointMetricName;
    private double minDistance;
    private ACSAlarm alarm;



    public BalanceStateRule() {
        subscribeTo(DistributionPointMetric.NAME);
        dstPointMetricName = DistributionPointMetric.NAME;
        minDistance = CrackerConfig.DST_POINT_MIN_DISTANCE;
        lastPoint = new DistributionPoint();
    }

    @Override
    public ACSAlarm getAlarm(MonitoringController monitoringController) {
        return alarm;
    }

    @Override
    public ACSAlarm verify(MonitoringController monitoringCtrl) {

        Wrapper<DistributionPoint> pointWrapper = monitoringCtrl.getValue(dstPointMetricName);

        if ( !pointWrapper.isValid() ) {
            alarm = ACSAlarm.ERROR;
        } else if (getDistance(lastPoint, pointWrapper.unwrap()) >= minDistance) {
            alarm = ACSAlarm.VIOLATION;
        } else {
            alarm = ACSAlarm.OK;
        }

        return alarm;
    }

    private double getDistance(DistributionPoint p1, DistributionPoint p2) {
        return Math.sqrt(Math.pow(p1.getX()-p2.getX(), 2) + Math.pow(p1.getY() - p2.getY(), 2));
    }
}
