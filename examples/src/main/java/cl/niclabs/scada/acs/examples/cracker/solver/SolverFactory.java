package cl.niclabs.scada.acs.examples.cracker.solver;

import cl.niclabs.scada.acs.component.factory.ACSFactory;
import cl.niclabs.scada.acs.component.factory.ACSFactoryException;
import cl.niclabs.scada.acs.examples.cracker.solver.components.*;
import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalContentException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.control.PABindingController;
import org.objectweb.proactive.core.component.control.PAContentController;
import org.objectweb.proactive.core.node.Node;

public class SolverFactory {

    private final ACSFactory factory;

    public SolverFactory(ACSFactory factory) {
        this.factory = factory;
    }

    public Component getSolver(String name) throws ACSFactoryException, IllegalLifeCycleException,
            NoSuchInterfaceException, IllegalContentException, IllegalBindingException {
        return getSolver(name, 1);
    }

    public Component getSolver(String name, int numberOfWorkers) throws ACSFactoryException, IllegalLifeCycleException,
            NoSuchInterfaceException, IllegalContentException, IllegalBindingException {
        return getSolver(name, numberOfWorkers, null);
    }

    public Component getSolver(String name, int numberOfWorkers, Node node) throws ACSFactoryException,
            NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException, IllegalContentException {

        Component solverComponent = factory.createCompositeComponent(name, getSolverType(), node);
        Component masterComponent = factory.createPrimitiveComponent("Master", getMasterType(), MasterImpl.class, node);

        PAContentController solverContentController = Utils.getPAContentController(solverComponent);
        solverContentController.addFcSubComponent(masterComponent);

        Utils.getPABindingController(solverComponent).bindFc(Solver.NAME, masterComponent.getFcInterface(Solver.NAME));
        PABindingController masterBindingController = Utils.getPABindingController(masterComponent);

        for (int i = 0; i < numberOfWorkers; i++) {

            String slaveName = "Slave".concat(String.valueOf(i));
            Component slave = factory.createPrimitiveComponent(slaveName, getSlaveType(), SlaveImpl.class, node);

            solverContentController.addFcSubComponent(slave);
            masterBindingController.bindFc(MulticastSlave.NAME, slave.getFcInterface(Slave.NAME));
        }

        ((MasterAttributes) GCM.getAttributeController(masterComponent)).setPartitionsNumber(numberOfWorkers);
        return solverComponent;
    }

    private ComponentType getSolverType() throws ACSFactoryException {
        return factory.createComponentType(new InterfaceType[] {
                factory.createInterfaceType(Solver.NAME, Solver.class, false, false)
        });
    }

    private ComponentType getMasterType() throws ACSFactoryException {
        return factory.createComponentType(new InterfaceType[]{
                factory.createInterfaceType(Solver.NAME, Solver.class, false, false),
                factory.createInterfaceType(Constants.ATTRIBUTE_CONTROLLER, MasterAttributes.class, false, false),
                factory.createInterfaceType(MulticastSlave.NAME, MulticastSlave.class, true, false, "multicast")
        });
    }

    private ComponentType getSlaveType() throws ACSFactoryException {
        return factory.createComponentType(new InterfaceType[]{
                factory.createInterfaceType(Slave.NAME, Slave.class, false, false)
        });
    }
}
