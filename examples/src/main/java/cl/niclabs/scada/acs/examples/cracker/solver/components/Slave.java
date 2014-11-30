package cl.niclabs.scada.acs.examples.cracker.solver.components;


import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.examples.cracker.solver.SolverTask;

public interface Slave {

	static final String NAME = "slave-itf";

	/**
	 * Tries to solve the task.
	 * @param solverTask The task to solve
	 * @return A valid wrapper with the password if it success, a wrong wrapper otherwise
	 */
	Wrapper<String> workOn(SolverTask solverTask);
}
