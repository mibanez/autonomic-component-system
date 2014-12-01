package cl.niclabs.scada.acs.examples.cracker.dispatcher.plans;


import cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm;
import cl.niclabs.scada.acs.component.controllers.execution.ExecutionController;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.planning.Plan;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.examples.cracker.common.components.Cracker;
import cl.niclabs.scada.acs.examples.cracker.common.components.Solver;
import cl.niclabs.scada.acs.examples.cracker.common.metrics.AvgResponseTime;
import cl.niclabs.scada.acs.examples.cracker.dispatcher.rules.MinimumResponseTime;

import java.util.Comparator;
import java.util.PriorityQueue;

import static cl.niclabs.scada.acs.examples.cracker.common.CrackerConfig.DEFAULT_SOLVER_SLOTS;


public class ResourcesProvision extends Plan {

    public static final String NAME = "resources-provision-plan";


    public ResourcesProvision() {
        subscribeTo(MinimumResponseTime.NAME);
    }

    @Override
    public void doPlanFor(String ruleName, ACSAlarm alarm, MonitoringController monitorCtrl,
            ExecutionController executionCtrl) {

        PriorityQueue<SolverTime> queue = new PriorityQueue<>(DEFAULT_SOLVER_SLOTS, new Comparator<SolverTime>() {
            @Override
            public int compare(SolverTime st1, SolverTime st2) {
                return (int) (st1.time - st2.time);
            }
        });

        for (int i = 0; i < DEFAULT_SOLVER_SLOTS; i++) {
            Wrapper<Long> wrapper = monitorCtrl.getValue(AvgResponseTime.NAME, new String[]{
                    Cracker.NAME,
                    Solver.NAME + "-" + i
            });
            if (wrapper.isValid()) {
                queue.add(new SolverTime(i, wrapper.unwrap()));
            }
        }

        SolverTime solverTime = queue.poll();

        //executionCtrl.execute("$this/child")


        System.out.println("[PLANS][RESOURCE_PROVISION] " + ruleName + " " + alarm);
    }

    class SolverTime {
        int index;
        long time;
        SolverTime(int i, long t) {
            index = i;
            time = t;
        }
    }
}
