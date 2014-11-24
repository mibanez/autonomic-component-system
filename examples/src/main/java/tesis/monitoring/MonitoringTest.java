package tesis.monitoring;

import cl.niclabs.scada.acs.component.ACSUtils;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;
import cl.niclabs.scada.acs.component.factory.ACSFactory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.control.PAContentController;
import tesis.monitoring.components.*;
import tesis.monitoring.metrics.CrackRequestCounter;
import tesis.monitoring.metrics.PendingRequests;
import tesis.monitoring.metrics.StoredPasswordCounter;

public class MonitoringTest {

    public static void main(String[] args) throws Exception {

        System.setProperty("gcm.provider", Fractive.class.getName());
        System.setProperty("java.security.policy", MonitoringTest.class.getResource("/proactive.java.policy").getPath());

        ACSFactory factory = new ACSFactory();

        ComponentType clientType = factory.createComponentType(new InterfaceType[]{
                factory.createInterfaceType(Client.NAME, Client.class.getName(), false, false),
                factory.createInterfaceType(Cracker.NAME, Cracker.class.getName(), true, false)
        });
        Component clientComponent = factory.createPrimitiveComponent("Client", clientType, ClientImpl.class, null);

        ComponentType compositeType = factory.createComponentType(new InterfaceType[]{
                factory.createInterfaceType(Cracker.NAME, Cracker.class.getName(), false, false)
        });
        Component compositeComponent = factory.createCompositeComponent("Composite", compositeType, null);

        ComponentType crackerType = factory.createComponentType(new InterfaceType[]{
                factory.createInterfaceType(Cracker.NAME, Cracker.class.getName(), false, false),
                factory.createInterfaceType(Repository.NAME, Repository.class.getName(), true, false)
        });
        Component crackerComponent = factory.createPrimitiveComponent("Cracker", crackerType, CrackerImpl.class, null);

        ComponentType repoType = factory.createComponentType(new InterfaceType[]{
                factory.createInterfaceType(Repository.NAME, Repository.class.getName(), false, false)
        });
        Component repoComponent = factory.createPrimitiveComponent("Repository", repoType, RepositoryImpl.class, null);

        System.out.println(">>> Adding cracker and repo to composite");
        PAContentController contentController = Utils.getPAContentController(compositeComponent);
        contentController.addFcSubComponent(crackerComponent);
        contentController.addFcSubComponent(repoComponent);

        System.out.println(">>> Binding components");
        Utils.getPABindingController(clientComponent).bindFc(Cracker.NAME, compositeComponent.getFcInterface(Cracker.NAME));
        Utils.getPABindingController(compositeComponent).bindFc(Cracker.NAME, crackerComponent.getFcInterface(Cracker.NAME));
        Utils.getPABindingController(crackerComponent).bindFc(Repository.NAME, repoComponent.getFcInterface(Repository.NAME));

        System.out.println(">>> Starting remote monitoring");
        Utils.getPAMembraneController(clientComponent).startMembrane();
        Utils.getPAMembraneController(crackerComponent).startMembrane();
        Utils.getPAMembraneController(repoComponent).startMembrane();
        Utils.getPAMembraneController(compositeComponent).startMembrane();
        ACSUtils.enableRemoteMonitoring(clientComponent);

        System.out.println(">>> Starting life-cycle");
        Utils.getPAGCMLifeCycleController(clientComponent).startFc();
        Utils.getPAGCMLifeCycleController(compositeComponent).startFc();

        System.out.println(">>> Adding metrics");
        ACSUtils.getMonitoringController(repoComponent).add(StoredPasswordCounter.NAME, StoredPasswordCounter.class);
        ACSUtils.getMonitoringController(crackerComponent).add(CrackRequestCounter.NAME, CrackRequestCounter.class);
        ACSUtils.getMonitoringController(compositeComponent).add(PendingRequests.NAME, PendingRequests.class);

        MonitoringController clientMon = ACSUtils.getMonitoringController(clientComponent);
        clientMon.startMonitoring();
        Thread.sleep(2000);


        Client client = (Client) clientComponent.getFcInterface(Client.NAME);
        client.start(128, 2000);

        Thread.sleep(100000000);
    }
}
