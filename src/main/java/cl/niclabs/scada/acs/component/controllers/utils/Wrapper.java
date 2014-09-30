package cl.niclabs.scada.acs.component.controllers.utils;

import java.io.Serializable;

public abstract class Wrapper<TYPE extends Serializable> implements Serializable {

    private final String message;

    Wrapper(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public abstract TYPE unwrap() throws WrongValueException;

}
