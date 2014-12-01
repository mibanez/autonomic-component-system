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

import static cl.niclabs.scada.acs.examples.cracker.common.CrackerConfig.DEFAULT_MAX_N_OF_SLAVES;
import static cl.niclabs.scada.acs.examples.cracker.common.CrackerConfig.DEFAULT_SOLVER_SLOTS;


public class ResourcesProvision extends Plan {

    public static final String NAME = "resources-provision-plan";

    private boolean success = false;
    private long initTime = 0;
    private long delay = 60000;

    public ResourcesProvision() {
        subscribeTo(MinimumResponseTime.NAME);
    }

    @Override
    public void doPlanFor(String ruleName, ACSAlarm alarm, MonitoringController monitorCtrl,
            ExecutionController executionCtrl) {

        if (success) {
            long time = (System.currentTimeMillis() - initTime);
            if (time > delay) {
                success = false;
            } else {
                System.out.println("[PLANS][RESOURCE_PROVISION] sleeping... " + time + "/" + delay);
                return;
            }
        }

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


        SolverTime worst = null;
        String msg = "[PLANS][RESOURCE_PROVISION] from (";
        while (!queue.isEmpty()) {
            SolverTime solverTime = queue.poll();
            Wrapper<Double> result = executionCtrl.execute("slaves-number($this/child::Solver" + solverTime.index + ");");
            if (!result.isValid()) {
                msg += "INVALID(" + result.getMessage() + "), ";
                continue;
            }
            msg += result.unwrap() + ", ";
            if (DEFAULT_MAX_N_OF_SLAVES > result.unwrap()) {
                worst = solverTime;
                while (!queue.isEmpty()) {
                    SolverTime solverTime2 = queue.poll();
                    Wrapper<Double> result2 = executionCtrl.execute("slaves-number($this/child::Solver" + solverTime2.index + ");");
                    msg += result2.isValid() ? result2.unwrap() : ("INVALID(" + result2.getMessage() + ")");
                    msg += ", ";
                }
            }
        }

        if (worst == null) {
            System.out.println(msg + ") selected none. Nothing to do");
            return;
        }

        msg += ") selected: " + worst.time + "(Solver" + worst.index + ")";
        Wrapper<Boolean> result = executionCtrl.execute("add-slave($this/child::Solver" + worst.index + ", $herculesApp);");
        if (!result.isValid() || !result.unwrap()) {
            System.out.println(msg + ": add-slave fail..." + result.getMessage());
            return;
        }

        System.out.println(msg + ": SUCCESS! sleeping for " + (delay/1000) + " seconds");
        initTime = System.currentTimeMillis();
        success = true;
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
