package cl.niclabs.scada.acs.examples.cracker.solver.components;

import org.objectweb.fractal.api.control.AttributeController;

/**
 * Attributes for Master Component
 *
 */
public interface MasterAttributes extends AttributeController {

	/**
	 * Number of partitions used to split the SolverTask
	 * (double is used because of gcm-script issues)
	 **/
	public void setPartitionsNumber(double number);
	public double setPartitionsNumber();
}
