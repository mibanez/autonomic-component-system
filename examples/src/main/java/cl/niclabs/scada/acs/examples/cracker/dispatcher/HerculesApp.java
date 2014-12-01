package cl.niclabs.scada.acs.examples.cracker.dispatcher;

import cl.niclabs.scada.acs.component.ACSManager;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.examples.cracker.common.CrackerConfig;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.extra.component.fscript.exceptions.ReconfigurationException;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class HerculesApp extends DispatcherAbstractApp {

    private final int INIT_N_OF_SOLVERS = 2;

    private GCMApplication herculesApp;

    public HerculesApp() throws ProActiveException {
        //File herculesAppDescriptor = new File(HerculesApp.class.getResource(HERCULES_DESCRIPTOR).getFile());
        herculesApp = PAGCMDeployment.loadApplicationDescriptor(CrackerConfig.class.getResource(
            "hercules-app.xml"));
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
    protected void extraConfiguration(Component cracker) throws MalformedURLException, URISyntaxException {
        try {
            ACSManager.getExecutionController(cracker).setGlobalVariable("herculesApp", herculesApp);
            String path = "/user/mibanez/memoria/autonomic-component-system/examples/src/main/"
                + "resources/cl/niclabs/scada/acs/examples/cracker/common/components/Solver.fscript";
            Wrapper<String[]> result = ACSManager.getExecutionController(cracker).load(path);

            if (result.isValid()) {
                for (String s : result.unwrap()) {
                    System.out.println("** load: " + s);
                }
            } else {
                System.out.println("**************:::: LOAD:::: " + result.getMessage());
            }
            for(String s : ACSManager.getExecutionController(cracker).getGlobals().unwrap()) {
                System.out.println("** global: " + s);
            }
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
        } catch (ReconfigurationException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        (new HerculesApp()).run();
    }

}
