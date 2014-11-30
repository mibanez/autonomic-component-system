package cl.niclabs.scada.acs.examples.cracker.dispatcher.components;

import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.examples.cracker.solver.SolverTask;

/**
 * Created by mibanez
 */
public interface Dispatcher {

    static final String NAME = "dispatcher-itf";

    Wrapper<String> dispatch(SolverTask solverTask);

}
