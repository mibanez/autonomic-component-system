package cl.niclabs.scada.acs.component.controllers.utils;

import java.io.Serializable;

/**
 * Created by mibanez
 */
public class Wrapper<TYPE extends Serializable> implements Serializable {

    private final TYPE value;
    private final String msg;
    private final Exception cause;

    /**
     * Wrapper constructor
     * @param object an object to wrap
     */
    public Wrapper(TYPE object) {
        this(object, "wrapped correctly");
    }

    public Wrapper(TYPE object, String message) {
        this(object, message, null);
    }

    public Wrapper(TYPE object, String message, Exception throwable) {
        value = object;
        msg = message;
        cause = throwable;
    }

    /**
     * Returns the wrapped value
     * @return the wrapped value
     */
    public TYPE unwrap() {
        return value;
    }

    /**
     * Returns the embedded message, thought to transmit error or exceptions avoiding the exceptions use.
     * Exceptions in GCM/ProActive forces synchronism.
     * @return the embedded message
     */
    public String getMessage() {
        return msg;
    }

    /**
     * Returns the exception thrown, if any.
     * @return the caught exception if any, null otherwise
     */
    public Exception getException() {
        return cause;
    }

}
