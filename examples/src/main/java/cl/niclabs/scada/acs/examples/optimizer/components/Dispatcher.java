package cl.niclabs.scada.acs.examples.optimizer.components;

import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;

/**
 * Created by mibanez
 */
public interface Dispatcher {

    static final String NAME = "dispatcher-itf";

    Wrapper<String> dispatch(Task task);

}
