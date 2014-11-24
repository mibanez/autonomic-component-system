package cl.niclabs.scada.acs.component.factory;

/**
 * Created by mibanez
 */
public class ACSFactoryException extends Exception {

    public ACSFactoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public ACSFactoryException(String message) {
        super(message);
    }

    public ACSFactoryException(Throwable cause) {
        super(cause);
    }

}
