package cl.niclabs.scada.acs.examples.cracker.autonomic;

import cl.niclabs.scada.acs.component.controllers.monitoring.events.RecordEvent;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.Metric;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.MetricStore;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.RecordStore;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.examples.cracker.CrackerConfig;
import cl.niclabs.scada.acs.examples.cracker.solver.component.Solver;
import tesis.monitoring.components.Cracker;


public class DistributionPointMetric extends Metric<DistributionPoint> {

    public static final String NAME = "distributionPoint";

    private final String[] s1Path = new String[] { Cracker.NAME, Solver.NAME + "-0" };
    private final String[] s2Path = new String[] { Cracker.NAME, Solver.NAME + "-1" };
    private final String[] s3Path = new String[] { Cracker.NAME, Solver.NAME + "-2" };
    private int counter = 0;

    private DistributionPoint lastPoint = new DistributionPoint();

    public DistributionPointMetric() {
        this.subscribeTo(RecordEvent.RESPONSE_RECEIVED);
    }
    @Override
    public DistributionPoint getValue() {
        return lastPoint;
    }

    @Override
    public DistributionPoint calculate(RecordStore recordStore, MetricStore metricStore) {

        if (++counter <= CrackerConfig.D_POINT_SKIPPED_REQUESTS) {
            return lastPoint;
        }
        counter = 0;

        Wrapper<Long> s1 = metricStore.getValue(AvgRespTimeMetric.NAME, s1Path);
        Wrapper<Long> s2 = metricStore.getValue(AvgRespTimeMetric.NAME, s2Path);
        Wrapper<Long> s3 = metricStore.getValue(AvgRespTimeMetric.NAME, s3Path);

        if ( !( s1.isValid() && s2.isValid() && s3.isValid() ) ) {
            String report = "\n" + s1.getMessage() + "\n" + s2.getMessage() + "\n" + s3.getMessage();
            System.err.println("[WARNING] DistributionPoint fail: " + report);
        } else if (s1.unwrap() <= 0 || s2.unwrap() <= 0 || s3.unwrap() <= 0) {
            // avoid broken values
            return lastPoint;
        }

        // x0, y0 = old points
        // x, y = new points
        // e1, e2, e3 = solvers efficiency

        // x0*e1 == solver 1 time =>
        double e1 = s1.unwrap() / lastPoint.getX();

        // (y0-x0)*e2 = solver 2 time =>
        double e2 = s2.unwrap() / (lastPoint.getY() - lastPoint.getX());

        // (1-y0)*e3 = time_s3
        double e3 = s3.unwrap() / (1 - lastPoint.getY());

        // We want all solvers take the same time, so
        // ---> x*e1 == (y-x)*e2 == (1-y)*e3

        // from 1 and 2: x == y*e2/(e1+e2) == y*k1
        double k1 = e2 / (e1 + e2);

        // from 1 and 3: x == (1-y)*e3/e1 == (1-y)*k2
        double k2 = e3/e1;

        // so, y == k2/(k1+k2) and x == y*k1
        double y = k2 / (k1 + k2);
        double x = y * k1;

        lastPoint = new DistributionPoint(x, y);
        System.out.println("[METRICS][DISTRIBUTION_POINT] " + lastPoint.toString());

        return lastPoint;
    }
}
