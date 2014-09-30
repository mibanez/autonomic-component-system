package cl.niclabs.scada.acs.component.controllers.utils;

/**
 * Created by mibanez
 */
public class WrongValueException extends Exception {

    public WrongValueException(String message) {
        super(message);
    }

    public WrongValueException(String message, Throwable cause) {
        super(message, cause);
    }

}
