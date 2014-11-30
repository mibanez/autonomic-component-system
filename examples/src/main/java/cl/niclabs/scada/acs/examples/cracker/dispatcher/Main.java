package cl.niclabs.scada.acs.examples.cracker.dispatcher;

import cl.niclabs.scada.acs.component.ACSException;
import cl.niclabs.scada.acs.component.ACSManager;
import cl.niclabs.scada.acs.component.controllers.DuplicatedElementIdException;
import cl.niclabs.scada.acs.component.controllers.InvalidElementException;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;
import cl.niclabs.scada.acs.component.factory.ACSFactory;
import cl.niclabs.scada.acs.component.factory.ACSFactoryException;
import cl.niclabs.scada.acs.examples.cracker.Client;
import cl.niclabs.scada.acs.examples.cracker.ClientFactory;
import cl.niclabs.scada.acs.examples.cracker.Cracker;
import cl.niclabs.scada.acs.examples.cracker.dispatcher.components.DispatcherImpl;
import cl.niclabs.scada.acs.examples.cracker.dispatcher.metrics.AvgResponseTime;
import cl.niclabs.scada.acs.examples.cracker.dispatcher.plans.ResourcesProvision;
import cl.niclabs.scada.acs.examples.cracker.dispatcher.rules.MinimumResponseTime;
import cl.niclabs.scada.acs.examples.cracker.solver.Solver;
import cl.niclabs.scada.acs.examples.cracker.solver.SolverFactory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalContentException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.control.PABindingController;
import org.objectweb.proactive.core.component.control.PAContentController;
import org.objectweb.proactive.core.component.identity.PAComponent;

import java.util.ArrayList;

/**
 * Created by mibanez
 */
public class Main {

    public static void main(String[] args) throws Exception {

        System.setProperty("gcm.provider", Fractive.class.getName());
        System.setProperty("java.security.policy", Main.class.getResource("/proactive.java.policy").getPath());

        ACSFactory factory = new ACSFactory();
        Component crackerComp = getCracker(factory, 2, 3);


        ClientFactory clientFactory = new ClientFactory(factory);
        Component clientComp = clientFactory.getClient("Client");
        Utils.getPABindingController(clientComp).bindFc(Cracker.NAME, crackerComp.getFcInterface(Cracker.NAME));

        Utils.getPAGCMLifeCycleController(crackerComp).startFc();
        Utils.getPAGCMLifeCycleController(clientComp).startFc();

        Client client = (Client) clientComp.getFcInterface(Client.NAME);

        System.out.println("CRACKER ON: " + ((PAComponent) crackerComp).getID().toString());
        System.out.println("=================================================MAIN==============================");
        client.start(DispatcherConfig.ALPHABET, DispatcherConfig.PASSWORD_MAX_LENGTH, 128, 0);

        while (true) {
            Thread.sleep(Long.MAX_VALUE);
        }
    }



    private static Component getCracker(ACSFactory factory, int numberOfSolvers, int numberOfWorkers)
            throws ACSFactoryException, NoSuchInterfaceException, IllegalLifeCycleException, IllegalContentException,
            IllegalBindingException, ACSException {

        Component cracker = factory.createCompositeComponent("Cracker", getCrackerType(factory), null);
        Component dispatcher = factory.createPrimitiveComponent("Dispatcher", getDispatcherType(factory),
                DispatcherImpl.class, null);

        PAContentController crackerContentController = Utils.getPAContentController(cracker);
        crackerContentController.addFcSubComponent(dispatcher);

        Utils.getPABindingController(cracker).bindFc(Cracker.NAME, dispatcher.getFcInterface(Cracker.NAME));
        PABindingController dispatcherBindingController = Utils.getPABindingController(dispatcher);

        SolverFactory solverFactory = new SolverFactory(factory);
        for (int i = 0; i < numberOfSolvers; i++) {
            Component solver = solverFactory.getSolver("Solver" + i, numberOfWorkers, null);
            crackerContentController.addFcSubComponent(solver);
            dispatcherBindingController.bindFc(Solver.NAME + "-" + i, solver.getFcInterface(Solver.NAME));

            ACSManager.getMonitoringController(solver).add(AvgResponseTime.NAME, AvgResponseTime.class);
        }

        ACSManager.enableRemoteMonitoring(cracker);

        MonitoringController crackerMon = ACSManager.getMonitoringController(cracker);
        crackerMon.add(AvgResponseTime.NAME, AvgResponseTime.class);
        crackerMon.startMonitoring();

        try {
            ACSManager.getAnalysisController(cracker).add(MinimumResponseTime.NAME, MinimumResponseTime.class);
            ACSManager.getPlanningController(cracker).add(ResourcesProvision.NAME, ResourcesProvision.class);
        } catch (DuplicatedElementIdException | InvalidElementException e) {
            e.printStackTrace();
        }
        return cracker;
    }

    private static ComponentType getCrackerType(ACSFactory factory) throws ACSFactoryException {
        return factory.createComponentType(new InterfaceType[] {
                factory.createInterfaceType(Cracker.NAME, Cracker.class, false, false)
        });
    }

    private static ComponentType getDispatcherType(ACSFactory factory) throws ACSFactoryException {
        ArrayList<InterfaceType> interfaceTypes = new ArrayList<>();
        interfaceTypes.add(factory.createInterfaceType(Cracker.NAME, Cracker.class, false, false));
        for (int i = 0; i < DispatcherImpl.SOLVER_SLOTS; i++) {
            interfaceTypes.add(factory.createInterfaceType(Solver.NAME + "-" + i, Solver.class, true, true));
        }
        return factory.createComponentType(interfaceTypes.toArray(new InterfaceType[interfaceTypes.size()]));
    }


    // CLIENT

}
