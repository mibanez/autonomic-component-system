package cl.niclabs.scada.acs.component.controllers;

/**
 * Created by mibanez
 */
public class ElementNotFoundException extends Exception {

    public ElementNotFoundException(String msg) {
        super(msg);
    }

    public ElementNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
