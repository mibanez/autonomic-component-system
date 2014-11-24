package cl.niclabs.scada.acs.component;

public class ACSException extends Exception {

    ACSException(String msg, Throwable cause) {
        super (msg, cause);
    }
}
