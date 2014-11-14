package cl.niclabs.scada.acs.component.controllers.utils;

import java.io.Serializable;


public class ValidWrapper<TYPE extends Serializable> implements Wrapper<TYPE> {

    private final TYPE value;
    private final String msg;

    public ValidWrapper(TYPE value) {
        this(value, "Correctly wrapped.");
    }

    public ValidWrapper(TYPE value, String msg) {
        this.value = value;
        this.msg = msg;
    }

    @Override
    public TYPE unwrap() {
        return value;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public String getMessage() {
        return msg;
    }
}
