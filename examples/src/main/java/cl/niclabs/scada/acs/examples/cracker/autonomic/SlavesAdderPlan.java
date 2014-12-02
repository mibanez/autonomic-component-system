package cl.niclabs.scada.acs.examples.cracker.autonomic;


import cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm;
import cl.niclabs.scada.acs.component.controllers.execution.ExecutionController;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.planning.Plan;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.examples.cracker.CrackerConfig;
import cl.niclabs.scada.acs.examples.cracker.solver.component.Solver;

import java.util.PriorityQueue;
import java.util.Queue;


public class SlavesAdderPlan extends Plan {

    public static final String NAME = "slavesAdder";

    private final String[] s1Path = new String[] { tesis.monitoring.components.Cracker.NAME, Solver.NAME + "-0" };
    private final String[] s2Path = new String[] { tesis.monitoring.components.Cracker.NAME, Solver.NAME + "-1" };
    private final String[] s3Path = new String[] { tesis.monitoring.components.Cracker.NAME, Solver.NAME + "-2" };

    private boolean sleeping = false;
    private long sleepStartTime = 0;

    public SlavesAdderPlan() {
        subscribeTo(MinRespTimeRule.NAME);
    }

    @Override
    public void doPlanFor(String ruleName, ACSAlarm alarm, MonitoringController monitorCtrl,
            ExecutionController executionCtrl) {

        if (stillSleeping()) {
            return;
        }

        Wrapper<Long> s1 = monitorCtrl.getValue(AvgRespTimeMetric.NAME, s1Path);
        Wrapper<Long> s2 = monitorCtrl.getValue(AvgRespTimeMetric.NAME, s2Path);
        Wrapper<Long> s3 = monitorCtrl.getValue(AvgRespTimeMetric.NAME, s3Path);

        if (areValidValues(s1, s2, s3)) {

            Queue<Pair> queue = new PriorityQueue<>();
            queue.add(new Pair(0, s1.unwrap()));
            queue.add(new Pair(1, s2.unwrap()));
            queue.add(new Pair(2, s3.unwrap()));

            for (Pair pair : queue) {
                Wrapper<Double> result = executionCtrl.execute("slaves-number($this/child::Solver" + pair.index + ");");
                if (result.unwrap().intValue() < CrackerConfig.MAX_SLAVES) {
                    executionCtrl.execute("add-slave($this/child::Solver" + pair.index + ");");
                    goToSleep();
                    System.out.println("[PLANS][SLAVES_ADDER] New solver Added on Solver" + pair.index + " now: " + (result.unwrap() + 1));
                    return;
                }
            }
        }
    }

    private void goToSleep() {
        sleeping = true;
        sleepStartTime = System.currentTimeMillis();
    }

    private boolean stillSleeping() {
        if (sleeping && (System.currentTimeMillis() - sleepStartTime) > CrackerConfig.SLEEP_TIME) {
            sleeping = false;
        }
        return sleeping;
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

    class Pair implements Comparable<Pair> {
        int index;
        long time;
        Pair (int i, long t) {
            index = i;
            time = t;
        }
        @Override
        public int compareTo(Pair o) {
            return (int) (o.time - time);
        }
    }
}
