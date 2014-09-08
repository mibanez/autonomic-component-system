package cl.niclabs.scada.acs.component.controllers.exceptions;

/**
 * Created by mibanez on 07-09-14.
 */
public class ACSIntegrationException extends Exception {

    public ACSIntegrationException() {
        super();
    }

    public ACSIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ACSIntegrationException(String message) {
        super(message);
    }

    public ACSIntegrationException(Throwable cause) {
        super(cause);
    }

}
