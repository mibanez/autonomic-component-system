package cl.niclabs.scada.acs.examples.cracker;

import org.mockito.Mockito;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


public class LocalApp extends AbstractApp {

    @Override
    protected String getBalancerScript() {
        return LocalApp.class.getResource("/cl/niclabs/scada/acs/examples/cracker/autonomic/balancer.fscript").getPath();
    }

    @Override
    protected String getSolverScript() {
        return LocalApp.class.getResource("/cl/niclabs/scada/acs/examples/cracker/autonomic/solver.fscript").getPath();
    }

    @Override
    protected GCMApplication getGCMApplication() {

        GCMVirtualNode vNode = Mockito.mock(GCMVirtualNode.class);
        Mockito.doReturn(null).when(vNode).getANode();

        GCMApplication app = Mockito.mock(GCMApplication.class);
        Mockito.doReturn(vNode).when(app).getVirtualNode(Mockito.anyString());

        return app;
    }

    public static void main(String[] args) throws Exception {
        (new LocalApp()).run();
    }
}
