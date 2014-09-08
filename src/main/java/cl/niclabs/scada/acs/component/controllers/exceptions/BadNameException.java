package cl.niclabs.scada.acs.component.controllers.exceptions;

/**
 * Created by mibanez on 07-09-14.
 */
public class BadNameException extends Exception {

    public BadNameException() {
        super();
    }

    public BadNameException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadNameException(String message) {
        super(message);
    }

    public BadNameException(Throwable cause) {
        super(cause);
    }

}
