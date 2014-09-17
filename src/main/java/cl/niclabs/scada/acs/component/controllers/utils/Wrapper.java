package cl.niclabs.scada.acs.component.controllers.utils;

import java.io.Serializable;

/**
 * Created by mibanez
 */
public class Wrapper<TYPE extends Serializable> implements Serializable {

    private final TYPE value;
    private final String msg;

    /**
     * Wrapper constructor
     * @param object an object to wrap
     */
    public Wrapper(TYPE object) {
        this(object, "wrapped correctly");
    }

    public Wrapper(TYPE object, String message) {
        value = object;
        msg = message;
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

}
