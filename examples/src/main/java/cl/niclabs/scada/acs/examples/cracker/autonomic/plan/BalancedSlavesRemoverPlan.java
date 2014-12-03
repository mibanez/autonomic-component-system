package cl.niclabs.scada.acs.examples.cracker.autonomic.plan;

import cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm;
import cl.niclabs.scada.acs.component.controllers.execution.ExecutionController;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.planning.Plan;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.examples.cracker.CrackerConfig;
import cl.niclabs.scada.acs.examples.cracker.autonomic.metric.DistributionPoint;
import cl.niclabs.scada.acs.examples.cracker.autonomic.metric.DistributionPointMetric;
import cl.niclabs.scada.acs.examples.cracker.autonomic.rule.MinRespTimeRule;

import java.util.PriorityQueue;
import java.util.Queue;

import static cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm.ERROR;
import static cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm.VIOLATION;

public class BalancedSlavesRemoverPlan extends Plan {

    public static final String NAME = "balancedSlavesRemover";

    private boolean sleeping = false;
    private long sleepStartTime = 0;
    private boolean empty = false;

    public BalancedSlavesRemoverPlan() {
        subscribeTo(MinRespTimeRule.NAME);
    }

    @Override
    public void doPlanFor(String ruleName, ACSAlarm alarm, MonitoringController monitorCtrl, ExecutionController executionCtrl) {

        if (alarm == ERROR) {
            System.out.println("[WARNING] SlavesAdder: ERROR alarm received from " + ruleName);
        } else if (alarm == VIOLATION) {

            if (stillSleeping()) {
                return;
            }

            // Sort, first the one with more assigned work
            Wrapper<DistributionPoint> point = monitorCtrl.getValue(DistributionPointMetric.NAME);
            if (point.isValid()) {

                double x = point.unwrap().getX(), y = point.unwrap().getY();

                Queue<Pair> queue = new PriorityQueue<>();
                queue.add(new Pair(0, x));
                queue.add(new Pair(1, y - x));
                queue.add(new Pair(2, 1 - y));

                for (Pair pair : queue) {

                    Wrapper<Boolean> removalResult = executionCtrl.execute(
                            "remote-execute($this/sibling::Solver" + pair.index + ", \"remove-slave($this)\")"
                    );

                    if (removalResult.isValid()) {
                        if (removalResult.unwrap()) {
                            goToSleep();
                            System.out.println("[ACTION] Slave removed on Solver" + pair.index);
                            return;
                        }
                        // min solvers reached
                    }
                    // System.out.println("[WARNING] couldn't remove slave from Solver" + pair.index);
                }

                if (!empty) {
                    empty = true;
                    System.out.println("[WARNING] Solvers are empty");
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

    class Pair implements Comparable<Pair> {
        int index;
        double time;
        Pair (int i, double t) {
            index = i;
            time = t;
        }
        @Override
        public int compareTo(Pair o) {
            double d = (o.time - time);
            return d > 0 ? 1 : d < 0 ? -1 : 0; // NOTE THIS
        }
    }
}
