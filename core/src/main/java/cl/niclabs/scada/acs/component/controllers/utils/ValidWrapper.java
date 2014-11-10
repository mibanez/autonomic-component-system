package cl.niclabs.scada.acs.component.controllers.utils;

import cl.niclabs.scada.acs.component.controllers.Wrapper;

import java.io.Serializable;

/**
 * Created by mibanez
 */
public class ValidWrapper<TYPE extends Serializable> implements Wrapper<TYPE> {

    private final TYPE value;

    public ValidWrapper(TYPE value) {
        this.value = value;
    }

    @Override
    public TYPE unwrap() {
        return value;
    }

}
