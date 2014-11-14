package cl.niclabs.scada.acs.component.controllers.utils;

import java.io.Serializable;


public class WrongWrapper<TYPE extends Serializable> implements Wrapper<TYPE> {

    private final String msg;

    public WrongWrapper(String msg) {
        this.msg = msg;
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
}
