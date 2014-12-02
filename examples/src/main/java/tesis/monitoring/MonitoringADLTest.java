package tesis.monitoring;

import cl.niclabs.scada.acs.component.ACSManager;
import cl.niclabs.scada.acs.component.adl.ACSAdlFactory;
import cl.niclabs.scada.acs.component.adl.ACSAdlFactoryFactory;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;
import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.control.PAContentController;
import tesis.monitoring.components.Client;
import tesis.monitoring.components.Cracker;
import tesis.monitoring.components.Repository;
import tesis.monitoring.metrics.CrackRequestCounter;
import tesis.monitoring.metrics.PendingRequests;
import tesis.monitoring.metrics.StoredPasswordCounter;

/**
 * Created by Matias on 02-12-2014.
 */
public class MonitoringADLTest {
    public static void main(String[] args) throws Exception {

        System.setProperty("gcm.provider", Fractive.class.getName());
        System.setProperty("java.security.policy", MonitoringTest.class.getResource("/proactive.java.policy").getPath());

        ACSAdlFactory factory = ACSAdlFactoryFactory.getACSAdlFactory();
        Component compositeComponent = factory.newACSComponent("tesis.monitoring.components.Composite", null);
        Component crackerComponent = factory.newACSComponent("tesis.monitoring.components.Cracker", null);
        Component repoComponent = factory.newACSComponent("tesis.monitoring.components.Repository", null);
        Component clientComponent = factory.newACSComponent("tesis.monitoring.components.Client", null);

        System.out.println(">>> Adding cracker and repo to composite");
        PAContentController contentController = Utils.getPAContentController(compositeComponent);
        contentController.addFcSubComponent(crackerComponent);
        contentController.addFcSubComponent(repoComponent);

        System.out.println(">>> Binding components");
        Utils.getPABindingController(clientComponent).bindFc(Cracker.NAME, compositeComponent.getFcInterface(Cracker.NAME));
        Utils.getPABindingController(compositeComponent).bindFc(Cracker.NAME, crackerComponent.getFcInterface(Cracker.NAME));
        Utils.getPABindingController(crackerComponent).bindFc(Repository.NAME, repoComponent.getFcInterface(Repository.NAME));

        System.out.println(">>> Starting remote monitoring");
        ACSManager.enableRemoteMonitoring(clientComponent);

        System.out.println(">>> Starting life-cycle");
        Utils.getPAGCMLifeCycleController(clientComponent).startFc();
        Utils.getPAGCMLifeCycleController(compositeComponent).startFc();

        System.out.println(">>> Adding metrics");
        ACSManager.getMonitoringController(repoComponent).add(StoredPasswordCounter.NAME, StoredPasswordCounter.class);
        ACSManager.getMonitoringController(crackerComponent).add(CrackRequestCounter.NAME, CrackRequestCounter.class);
        ACSManager.getMonitoringController(compositeComponent).add(PendingRequests.NAME, PendingRequests.class);

        MonitoringController clientMon = ACSManager.getMonitoringController(clientComponent);
        clientMon.startMonitoring();
        Thread.sleep(2000);


        Client client = (Client) clientComponent.getFcInterface(Client.NAME);
        client.start(128, 2000);

        Thread.sleep(100000000);
    }
}
