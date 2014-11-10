package cl.niclabs.scada.acs.component.controllers;

/**
 * Created by mibanez
 */
public class InvalidElementException extends Exception {

    public InvalidElementException(String msg) {
        super(msg);
    }

    public InvalidElementException(Throwable cause) {
        super(cause);
    }

    public InvalidElementException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
