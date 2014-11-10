package cl.niclabs.scada.acs.examples.optimizer.components;

import cl.niclabs.scada.acs.component.controllers.Wrapper;

/**
 * Created by mibanez
 */
public interface Dispatcher {

    Wrapper<String> dispatch(Task task);

}
