package cl.niclabs.scada.acs.examples.cracker.autonomic;

import cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm;
import cl.niclabs.scada.acs.component.controllers.execution.ExecutionController;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.planning.Plan;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;

/**
 * Created by Matias on 01-12-2014.
 */
public class BalanceUpdaterPlan extends Plan {

    public static final String NAME = "balanceUpdater";

    public BalanceUpdaterPlan () {
        subscribeTo(BalanceStateRule.NAME);
    }

    @Override
    public void doPlanFor(String ruleName, ACSAlarm alarm, MonitoringController monitorCtrl, ExecutionController executionCtrl) {
        Wrapper<DistributionPoint> wrapper = monitorCtrl.getValue(DistributionPointMetric.NAME);
        executionCtrl.execute("set-value($this/child::Balancer/attribute::x, " + wrapper.unwrap().getX() + ");");
        executionCtrl.execute("set-value($this/child::Balancer/attribute::y, " + wrapper.unwrap().getY() + ");");
        //System.out.println("[PLANS][BALANCER_UPDATER] distribution point updated.");
    }
}
