package cl.niclabs.scada.acs.examples.cracker.dispatcher;

import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.node.Node;


public class LocalApp extends DispatcherAbstractApp {

    @Override
    protected Node[] getSolverNodes() {
        return new Node[] { null, null };
    }

    @Override
    protected void configureSolver(Component solvers) {
        // nada
    }

    public static void main(String[] args) throws Exception {
        (new LocalApp()).run();
    }
}
