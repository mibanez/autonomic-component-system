package cl.niclabs.scada.acs.gcmscript.console;


public interface Command {

    String getName();
    String getInfo();

    Object execute(Console console, String args) throws CommandException;
}
