package cl.niclabs.scada.acs.component.controllers.utils;

import java.io.Serializable;

public class WrongWrapper<TYPE extends Serializable> extends Wrapper<TYPE> {

    private final WrongValueException cause;

    public WrongWrapper(String message) {
        super(message);
        this.cause = new WrongValueException(message);
    }

    public WrongWrapper(String message, Throwable cause) {
        super(message);
        this.cause = new WrongValueException(message, cause);
    }

    @Override
    public TYPE unwrap() throws WrongValueException {
       throw this.cause;
    }

}
