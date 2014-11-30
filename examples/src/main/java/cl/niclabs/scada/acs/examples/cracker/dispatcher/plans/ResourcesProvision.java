package cl.niclabs.scada.acs.examples.cracker.dispatcher.plans;


import cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm;
import cl.niclabs.scada.acs.component.controllers.execution.ExecutionController;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.planning.Plan;
import cl.niclabs.scada.acs.examples.cracker.dispatcher.rules.MinimumResponseTime;

public class ResourcesProvision extends Plan {

    public static final String NAME = "resources-provision-plan";

    public ResourcesProvision() {
        subscribeTo(MinimumResponseTime.NAME);
    }

    @Override
    public void doPlanFor(String ruleName, ACSAlarm alarm, MonitoringController monitorCtrl,
            ExecutionController executionCtrl) {

        //for (DispatcherImpl.SOLVER_SLOTS
        //executionCtrl.execute("$this/child")


        System.out.println("[PLANS][RESOURCE_PROVISION] " + ruleName + " " + alarm);
    }
}
