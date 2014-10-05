package cl.niclabs.scada.acs.component.controllers.utils;

import cl.niclabs.scada.acs.component.controllers.CommunicationException;
import cl.niclabs.scada.acs.component.controllers.Wrapper;

import java.io.Serializable;

public class WrongWrapper<TYPE extends Serializable> implements Wrapper<TYPE> {

    private final CommunicationException cause;

    public WrongWrapper(Throwable cause) {
        this.cause = new CommunicationException(cause);
    }

    @Override
    public TYPE unwrap() throws CommunicationException {
       throw this.cause;
    }

}
