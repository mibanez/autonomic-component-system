package cl.niclabs.scada.acs.component.controllers.utils;

import java.io.Serializable;

/**
 * Created by mibanez
 */
public class ValidWrapper<TYPE extends Serializable> extends Wrapper<TYPE> {

    private final TYPE value;

    public ValidWrapper(TYPE value) {
        super("(no message)");
        this.value = value;
    }

    public ValidWrapper(TYPE value, String message) {
        super(message);
        this.value = value;
    }

    @Override
    public TYPE unwrap() throws WrongValueException {
        return value;
    }

}
