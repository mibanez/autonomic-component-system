package cl.niclabs.scada.acs.component.controllers;

/**
 * Created by mibanez
 */
public class DuplicatedElementIdException extends Exception {

    public DuplicatedElementIdException(String msg) {
        super(msg);
    }

    public DuplicatedElementIdException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
