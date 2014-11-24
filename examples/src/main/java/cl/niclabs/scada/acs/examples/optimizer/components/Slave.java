package cl.niclabs.scada.acs.examples.optimizer.components;


import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;

public interface Slave {

	public Wrapper<String> workOn(Task task);

}
