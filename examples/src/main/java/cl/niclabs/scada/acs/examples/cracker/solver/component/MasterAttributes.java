package cl.niclabs.scada.acs.examples.cracker.solver.component;

import org.objectweb.fractal.api.control.AttributeController;

/**
 * Attributes for Master Component
 *
 */
public interface MasterAttributes extends AttributeController {

	double getPartitionsNumber();

	/**
	 * Number of partitions used to split the SolverTask
	 * (double is used because of gcm-script issues)
	 **/
	void setPartitionsNumber(double number);

}
