package cl.niclabs.scada.acs.examples.cracker.common.components;

import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;

/**
 * Solves a solverTask
 *
 */
public interface Solver {

    static final String NAME = "solver-itf";

    Wrapper<String> solve(SolverTask solverTask);

}
