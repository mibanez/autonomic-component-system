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

    private DistributionPoint lastPoint;
    private String avgRespTimeMetric;
    private int numberOfSkipped;
    private int skippedCounter = 0;
    private long initTime = 0;

    private int testLogCounter = 0;


    public DistributionPointMetric() {
        this.lastPoint = new DistributionPoint();
        this.numberOfSkipped = CrackerConfig.SKIPPED_CALCULATIONS;
        this.avgRespTimeMetric = AvgRespTimeMetric.NAME;

        setEnabled(true);
        subscribeTo(RecordEvent.RESPONSE_RECEIVED);
    }

    @Override
    public DistributionPoint getValue() {
        return lastPoint;
    }

    @Override
    public DistributionPoint calculate(RecordStore recordStore, MetricStore metricStore) {

        if (initTime <= 0) {
            initTime = System.currentTimeMillis();
            testLogCounter = 0;
            System.out.println("[0] time,s1_time,s2_time,s3_time,s1_assignment,s2_assignment,s3_assignment");
        }

        Wrapper<Long> s1 = metricStore.getValue(avgRespTimeMetric, s1Path);
        Wrapper<Long> s2 = metricStore.getValue(avgRespTimeMetric, s2Path);
        Wrapper<Long> s3 = metricStore.getValue(avgRespTimeMetric, s3Path);

        if (areValidValues(s1, s2, s3) && shouldCalculate()) {
            lastPoint = calculateNewPoint(lastPoint, s1.unwrap(), s2.unwrap(), s3.unwrap());
        }

        String testLogMsg = "[" + ++testLogCounter + "] " + String.valueOf(System.currentTimeMillis() - initTime);
        testLogMsg += "," + s1.unwrap() + "," + s2.unwrap() + "," + s3.unwrap();
        System.out.println(testLogMsg + "," + lastPoint.asTestLog());

        return lastPoint;
    }

    private DistributionPoint calculateNewPoint(DistributionPoint lastPoint, long t1, long t2, long t3) {

        // Lets "wi" be the percentage of work assigned to solver i.
        double w1 = lastPoint.getX();
        double w2 = lastPoint.getY() - lastPoint.getX();
        double w3 = 1 - lastPoint.getY();

        // Lets define the power "pi" of a solver as the work
        // divided by the time needed to solve it.
        double p1 = w1/t1, p2 = w2/t2, p3 = w3/t3;

        // We want equals time on each solver, by modifying the
        // percentage of work assigned. So we search for a new
        // work assignment wi' in order to have new times ti'
        // such that:
        //       t1' == t2' == t3'
        //  <=>  w1'/p1 == w2'/p2 == w3'/p3
        //
        // Using the distribution point notation:
        //  <=>  x/p1 == (y - x)/p2 == (1 - y)/p3
        //   =>  x(p2/p1) + x == y  &&  x(p3/p1) + y == 1
        //   =>  x == p1/(p1 + p2 + p3) && y == (p1 + p2)/(p1 + p2 + p3)

        return new DistributionPoint(p1/(p1 + p2 + p3), (p1 + p2)/(p1 + p2 + p3));
    }

    private boolean shouldCalculate() {
        if (++skippedCounter > numberOfSkipped) {
            skippedCounter = 0;
            return true;
        }
        return false;
    }

    private boolean areValidValues(Wrapper<Long> s1, Wrapper<Long> s2, Wrapper<Long> s3) {
        if ( !( s1.isValid() && s2.isValid() && s3.isValid() ) ) {
            String report = "\n" + s1.getMessage() + "\n" + s2.getMessage() + "\n" + s3.getMessage();
            System.err.println("[WARNING] DistributionPoint fail: " + report);
            return false;
        } else if (s1.unwrap() <= 0 || s2.unwrap() <= 0 || s3.unwrap() <= 0) {
            // avoid broken values
            return false;
        }
        return true;
    }
}
