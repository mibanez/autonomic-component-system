package cl.niclabs.scada.acs.gcmscript.console;

public class CommandException extends Exception {

    public CommandException(String msg) {
        super(msg);
    }

    public CommandException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
