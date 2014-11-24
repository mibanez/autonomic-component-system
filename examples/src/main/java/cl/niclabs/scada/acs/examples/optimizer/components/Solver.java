package cl.niclabs.scada.acs.examples.optimizer.components;

import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;

/**
 * Created by mibanez
 */
public interface Solver {

    Wrapper<String> solve(Task task);

}
