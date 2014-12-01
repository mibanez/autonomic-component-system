package cl.niclabs.scada.acs.examples.cracker.dispatcher;

import cl.niclabs.scada.acs.component.ACSManager;
import cl.niclabs.scada.acs.component.adl.ACSAdlFactory;
import cl.niclabs.scada.acs.component.adl.ACSAdlFactoryFactory;
import cl.niclabs.scada.acs.examples.cracker.common.components.Solver;
import cl.niclabs.scada.acs.examples.cracker.common.metrics.AvgResponseTime;
import cl.niclabs.scada.acs.examples.cracker.dispatcher.metrics.DispatcherAvgResponseTime;
import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.adl.nodes.ADLNodeProvider;
import org.objectweb.proactive.core.component.control.PAContentController;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.core.node.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public abstract class DispatcherAbstractApp {

    private final String CRACKER_ADL = "cl.niclabs.scada.acs.examples.cracker.dispatcher.components.Cracker";
    private final String SOLVER_ADL = "cl.niclabs.scada.acs.examples.cracker.common.components.Solver";

    public void run() throws Exception {
        System.setProperty("gcm.provider", Fractive.class.getName());
        //System.setProperty("java.security.policy", DispatcherAbstractApp.class.getResource("proactive.java.policy").getPath());

        ACSAdlFactory adlFactory = (ACSAdlFactory) ACSAdlFactoryFactory.getACSAdlFactory();
        Component crackerComp = (Component) adlFactory.newACSComponent(CRACKER_ADL, null);

        PAContentController crackerContentCtrl = Utils.getPAContentController(crackerComp);
        Component dispatcherComp = crackerContentCtrl.getFcSubComponents()[0];

        Node[] solverNodes = getSolverNodes();

        for (int i = 0; i < solverNodes.length; i++) {

            Map<String, Object> context = new HashMap<>();
            if (solverNodes[i] != null) {
                ArrayList<Node> list = new ArrayList<>();
                list.add(solverNodes[i]);
                context.put(ADLNodeProvider.NODES_ID, list);
            }

            Component solverComp = (Component) adlFactory.newACSComponent(SOLVER_ADL, context);
            GCM.getNameController(solverComp).setFcName("Solver" + i);
            Utils.getPAContentController(crackerComp).addFcSubComponent(solverComp);
            Utils.getPABindingController(dispatcherComp).bindFc(Solver.NAME + "-" + i, solverComp.getFcInterface(Solver.NAME));

            ACSManager.getMonitoringController(solverComp).add(AvgResponseTime.NAME, AvgResponseTime.class);
        }

        ACSManager.getMonitoringController(crackerComp).add(DispatcherAvgResponseTime.NAME, DispatcherAvgResponseTime.class);
        ACSManager.enableRemoteMonitoring(crackerComp);
        Thread.sleep(3000);

        ACSManager.getMonitoringController(crackerComp).startMonitoring();
        Thread.sleep(3000);

        extraConfiguration(crackerComp);
        Utils.getPAGCMLifeCycleController(crackerComp).startFc();
        System.out.println("CRACKER ON: " + ((PAComponent) crackerComp).getID().toString());

        while (true) {
            Thread.sleep(Long.MAX_VALUE);
        }
    }

    protected abstract Node[] getSolverNodes();

    protected abstract void extraConfiguration(Component cracker);

}
