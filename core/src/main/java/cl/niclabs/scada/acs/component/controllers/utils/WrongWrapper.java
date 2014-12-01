package cl.niclabs.scada.acs.component.controllers.utils;

import java.io.Serializable;


public class WrongWrapper<TYPE extends Serializable> implements Wrapper<TYPE> {

    private final String msg;
    private Exception cause;

    public WrongWrapper(String msg) {
        this.msg = msg;
        this.cause = null;
    }

    public WrongWrapper(String msg, Exception cause) {
        this.msg = msg;
        this.cause = cause;
    }

    @Override
    public TYPE unwrap() {
       return null;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public String getMessage() {
        return msg;
    }

    public Exception getCause() {
        return cause;
    }
}
