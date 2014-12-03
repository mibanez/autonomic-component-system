package cl.niclabs.scada.acs.examples.cracker.autonomic.plan;


import cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm;
import cl.niclabs.scada.acs.component.controllers.execution.ExecutionController;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.planning.Plan;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.examples.cracker.CrackerConfig;
import cl.niclabs.scada.acs.examples.cracker.autonomic.metric.AvgRespTimeMetric;
import cl.niclabs.scada.acs.examples.cracker.autonomic.rule.MaxRespTimeRule;
import cl.niclabs.scada.acs.examples.cracker.solver.component.Solver;

import java.util.PriorityQueue;
import java.util.Queue;

import static cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm.ERROR;
import static cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm.VIOLATION;


public class SlavesAdderPlan extends Plan {

    public static final String NAME = "slavesAdder";

    private boolean sleeping = false;
    private long sleepStartTime = 0;
    private int maxNOfSlaves;


    public SlavesAdderPlan() {
        this.maxNOfSlaves = CrackerConfig.MAX_SLAVES;

        subscribeTo(MaxRespTimeRule.NAME);
    }

    @Override
    public void doPlanFor(String ruleName, ACSAlarm alarm, MonitoringController monitorCtrl,
            ExecutionController executionCtrl) {

        if (alarm == ERROR) {
            System.out.println("[WARNING] SlavesAdder: ERROR alarm received from " + ruleName);
        } else if (alarm == VIOLATION) {

            if (stillSleeping()) {
                return;
            }

            Wrapper<Long> s1 = monitorCtrl.getValue(AvgRespTimeMetric.NAME, new String[] { Solver.NAME + "-0" });
            Wrapper<Long> s2 = monitorCtrl.getValue(AvgRespTimeMetric.NAME, new String[] { Solver.NAME + "-1" });
            Wrapper<Long> s3 = monitorCtrl.getValue(AvgRespTimeMetric.NAME, new String[] { Solver.NAME + "-2" });

            if (areValidValues(s1, s2, s3)) {

                Queue<Pair> queue = new PriorityQueue<>();
                queue.add(new Pair(0, s1.unwrap()));
                queue.add(new Pair(1, s2.unwrap()));
                queue.add(new Pair(2, s3.unwrap()));

                addSlave(queue, executionCtrl);
            }
        }
    }

    private void addSlave(Queue<Pair> queue, ExecutionController executionCtrl) {

        for (Pair pair : queue) {

            Wrapper<Boolean> additionResult = executionCtrl.execute(
                    "remote-execute($this/sibling::Solver" + pair.index + ", \"add-slave($this)\")"
            );

            if (additionResult.isValid()) {
                if (additionResult.unwrap()) {
                    goToSleep();
                    System.out.println("[ACTION] Slave added on Solver" + pair.index);
                    return;
                }
                // max solvers reached
            }
            System.out.println("[WARNING] couldn't add slave to Solver" + pair.index);
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
