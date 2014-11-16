package cl.niclabs.scada.acs.gcmscript;

/**
 * Created by mibanez
 */
public class CommunicationException extends Exception {

    public CommunicationException(String msg) {
        super(msg);
    }

    public CommunicationException(Throwable cause) {
        super(cause);
    }

}
