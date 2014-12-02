package cl.niclabs.scada.acs.examples.cracker;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;

import java.net.URL;

public class HerculesApp extends AbstractApp {

    private GCMApplication herculesApp;

    public HerculesApp() throws ProActiveException {
        URL herculesAppDescriptor = HerculesApp.class.getResource("hercules-app.xml");
        herculesApp = PAGCMDeployment.loadApplicationDescriptor(herculesAppDescriptor);

    }

    @Override
    protected String getGCMScriptLib() {
        return "/user/mibanez/memoria/autonomic-component-system/examples/src/main/resources/"
            + "cl/niclabs/scada/acs/examples/cracker/autonomic/cracker.fscript";
    }

    @Override
    protected GCMApplication getGCMApplication() {
        return herculesApp;
    }

    public static void main(String[] args) throws Exception {
        (new HerculesApp()).run();
    }
}
