package cl.niclabs.scada.acs.component.controllers.execution;

import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import org.objectweb.proactive.extra.component.fscript.exceptions.ReconfigurationException;

import java.io.Serializable;

/**
 * Created by mibanez
 */
public interface ExecutionController {

    static final String ITF_NAME = "execution-controller-nf";

    /**
     * Loads procedure definitions from a file containing source code, and makes them available
     * for later invocation by name.
     *
     * @param fileName The name of the file containing the source code of the procedure definitions.
     * @return The names of all the procedures successfully loaded.
     * @throws ReconfigurationException If errors were detected in the procedure definitions.
     */
    Wrapper<String[]> load(String fileName) throws ReconfigurationException;

    /**
     * Returns the names of all the currently defined global variables.
     *
     * @return The names of all the currently defined global variables.
     * @throws ReconfigurationException If an error occurred while getting global variable names.
     */
    Wrapper<String[]> getGlobals() throws ReconfigurationException;

    /**
     * Executes a code fragment: either an FPath expression or a single FScript statement.
     *
     * @param source The code fragment to execute.
     * @return The value of the code fragment, if successfully executed.
     */
    public <REPLY extends Serializable> Wrapper<REPLY> execute(String source);

}
