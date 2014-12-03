package cl.niclabs.scada.acs.examples.cracker;

import cl.niclabs.scada.acs.component.ACSManager;
import cl.niclabs.scada.acs.component.adl.ACSAdlFactory;
import cl.niclabs.scada.acs.component.adl.ACSAdlFactoryFactory;
import cl.niclabs.scada.acs.component.controllers.analysis.AnalysisController;
import cl.niclabs.scada.acs.component.controllers.execution.ExecutionController;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.planning.PlanningController;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.examples.cracker.autonomic.metric.AvgRespTimeMetric;
import cl.niclabs.scada.acs.examples.cracker.autonomic.metric.DistributionPointMetric;
import cl.niclabs.scada.acs.examples.cracker.autonomic.metric.DummyDistributionPointMetric;
import cl.niclabs.scada.acs.examples.cracker.autonomic.plan.*;
import cl.niclabs.scada.acs.examples.cracker.autonomic.rule.BalanceStateRule;
import cl.niclabs.scada.acs.examples.cracker.autonomic.rule.MaxRespTimeRule;
import cl.niclabs.scada.acs.examples.cracker.autonomic.rule.MinRespTimeRule;
import cl.niclabs.scada.acs.examples.cracker.solver.component.Solver;
import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.adl.nodes.ADLNodeProvider;
import org.objectweb.proactive.core.component.control.PAContentController;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public abstract class AbstractApp {

    private final String CRACKER_ADL = "cl.niclabs.scada.acs.examples.cracker.component.Cracker";
    private final String SOLVER_ADL = "cl.niclabs.scada.acs.examples.cracker.solver.component.Solver";

    public void run() throws Exception {

        System.setProperty("gcm.provider", Fractive.class.getName());
        if (System.getProperty("java.security.policy") == null) {
            String securityPolicy = AbstractApp.class.getResource("/proactive.java.policy").getPath();
            System.setProperty("java.security.policy", securityPolicy);
        }

        ACSAdlFactory adlFactory = ACSAdlFactoryFactory.getACSAdlFactory();
        Component crackerComp = adlFactory.newACSComponent(CRACKER_ADL, null);
        GCM.getNameController(crackerComp).setFcName("Cracker");

        // BALANCER
        PAContentController crackerContentCtrl = Utils.getPAContentController(crackerComp);
        Component balancerComp = crackerContentCtrl.getFcSubComponents()[0];
        GCM.getNameController(balancerComp).setFcName("Balancer");

        MonitoringController BalancerMonitoringCtrl = ACSManager.getMonitoringController(balancerComp);
        BalancerMonitoringCtrl.add(AvgRespTimeMetric.NAME, AvgRespTimeMetric.class);
        BalancerMonitoringCtrl.add(DummyDistributionPointMetric.NAME, DummyDistributionPointMetric.class);
        BalancerMonitoringCtrl.add(DistributionPointMetric.NAME, DistributionPointMetric.class);

        AnalysisController BalancerAnalysisCtrl = ACSManager.getAnalysisController(balancerComp);
        BalancerAnalysisCtrl.add(MaxRespTimeRule.NAME, MaxRespTimeRule.class);
        BalancerAnalysisCtrl.add(MinRespTimeRule.NAME, MinRespTimeRule.class);
        BalancerAnalysisCtrl.add(BalanceStateRule.NAME, BalanceStateRule.class);

        PlanningController planningCtrl = ACSManager.getPlanningController(balancerComp);
        planningCtrl.add(SlavesAdderPlan.NAME, SlavesAdderPlan.class);
        planningCtrl.add(SlavesRemoverPlan.NAME, SlavesRemoverPlan.class);
        planningCtrl.add(BalancedSlavesAdderPlan.NAME, BalancedSlavesAdderPlan.class);
        planningCtrl.add(BalancedSlavesRemoverPlan.NAME, BalancedSlavesRemoverPlan.class);
        planningCtrl.add(BalanceUpdaterPlan.NAME, BalanceUpdaterPlan.class);

        ExecutionController executionCtrl = ACSManager.getExecutionController(balancerComp);
        Wrapper<String[]> loadResult = executionCtrl.load(getBalancerScript());
        if (loadResult.isValid()) {
            for (String s : loadResult.unwrap()) {
                System.out.println("Loaded on Balancer: " + s);
            }
        } else {
            System.out.println("Can't load gcmscript libs: " + loadResult.getMessage());
        }


        // SOLVERS
        GCMApplication gcmApp = getGCMApplication();
        gcmApp.startDeployment();
        gcmApp.waitReady();

        for (int i = 0; i < 3; i++) {
            GCMVirtualNode solverVirtualNode = gcmApp.getVirtualNode("SolverVN" + i);
            solverVirtualNode.waitReady();
            Node solverNode = solverVirtualNode.getANode();

            Map<String, Object> context = new HashMap<>();
            if (solverNode != null) {
                ArrayList<Node> list = new ArrayList<>();
                list.add(solverNode);
                context.put(ADLNodeProvider.NODES_ID, list);
            }

            Component solverComp = adlFactory.newACSComponent(SOLVER_ADL, context);
            GCM.getNameController(solverComp).setFcName("Solver" + i);

            Utils.getPAContentController(crackerComp).addFcSubComponent(solverComp);
            Utils.getPABindingController(balancerComp).bindFc(Solver.NAME + "-" + i,
                    solverComp.getFcInterface(Solver.NAME));

            ACSManager.getMonitoringController(solverComp).add(AvgRespTimeMetric.NAME, AvgRespTimeMetric.class);
            ExecutionController solverExecutionCtrl = ACSManager.getExecutionController(solverComp);
            loadResult = solverExecutionCtrl.load(getSolverScript());
            if (loadResult.isValid()) {
                for (String s : loadResult.unwrap()) {
                    System.out.println("Loaded on Solver" + i +": " + s);
                }
            } else {
                System.out.println("Can't load Solver" + i +": " + loadResult.getMessage());
            }
        }




        ACSManager.enableRemoteMonitoring(crackerComp);
        Thread.sleep(3000);

        ACSManager.getMonitoringController(crackerComp).startMonitoring();
        Thread.sleep(3000);

        Utils.getPAGCMLifeCycleController(crackerComp).startFc();
        System.out.println("CRACKER ON: " + ((PAComponent) crackerComp).getID().toString());

        while (true) {
            Thread.sleep(Long.MAX_VALUE);
        }
    }

    protected abstract String getBalancerScript();
    protected abstract GCMApplication getGCMApplication();
    protected abstract String getSolverScript();
}
