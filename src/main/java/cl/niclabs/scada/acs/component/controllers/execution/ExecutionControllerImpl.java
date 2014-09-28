package cl.niclabs.scada.acs.component.controllers.execution;

import cl.niclabs.scada.acs.component.controllers.ExecutionController;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import org.objectweb.proactive.core.component.componentcontroller.AbstractPAComponentController;
import org.objectweb.proactive.extra.component.fscript.exceptions.ReconfigurationException;

import java.io.Serializable;
import java.util.Set;

/**
 * Created by mibanez
 */
public class ExecutionControllerImpl extends AbstractPAComponentController implements ExecutionController {

    public static final String CONTROLLER_NAME = "ExecutionController";
    public static final String EXECUTION_CONTROLLER_SERVER_ITF = "execution-controller-server-itf-nf";

    @Override
    public Set<String> load(String fileName) throws ReconfigurationException {
        return null;
    }

    @Override
    public Set<String> getGlobals() throws ReconfigurationException {
        return null;
    }

    @Override
    public <REPLY extends Serializable> Wrapper<REPLY> execute(String source) {
        return null;
    }
}
