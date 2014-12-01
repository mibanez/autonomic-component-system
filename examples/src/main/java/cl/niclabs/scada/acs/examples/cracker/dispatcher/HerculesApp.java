package cl.niclabs.scada.acs.examples.cracker.dispatcher;

import cl.niclabs.scada.acs.component.ACSManager;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

import java.io.File;

public class HerculesApp extends DispatcherAbstractApp {

    private final String HERCULES_DESCRIPTOR = "/cl/niclabs/scada/acs/examples/cracker/common/hercules-app.xml";
    private final int INIT_N_OF_SOLVERS = 2;

    private GCMApplication herculesApp;

    public HerculesApp() throws ProActiveException {
        File herculesAppDescriptor = new File(HerculesApp.class.getResource(HERCULES_DESCRIPTOR).getFile());
        GCMApplication herculesApp = PAGCMDeployment.loadApplicationDescriptor(herculesAppDescriptor);
        herculesApp.startDeployment();
        herculesApp.waitReady();
    }

    @Override
    protected Node[] getSolverNodes() {
        // Initial conditions: 2 solvers, 1 slave each one
        Node[] solverNodes = new Node[INIT_N_OF_SOLVERS];
        for (int i = 0; i < INIT_N_OF_SOLVERS; i++) {
            GCMVirtualNode solverVirtualNode = herculesApp.getVirtualNode("SolverVN" + i);
            solverVirtualNode.waitReady();
            solverNodes[i] = solverVirtualNode.getANode();
        }
        return solverNodes;
    }

    @Override
    protected void configureSolver(Component solver) {
        try {
            ACSManager.getExecutionController(solver).setGlobalVariable("herculesApp", herculesApp);
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        (new HerculesApp()).run();
    }

}
