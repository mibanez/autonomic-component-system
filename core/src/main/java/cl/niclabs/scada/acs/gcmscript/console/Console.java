package cl.niclabs.scada.acs.gcmscript.console;


import cl.niclabs.scada.acs.component.controllers.execution.ExecutionController;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;

import java.util.Collection;

public interface Console {

    void printError(String error);
    void printMessage(String msg);
    void printColumns(Collection<String> items);

    Object execute(String commandName, String arg);
    Object execute(Command command, String arg);
    Collection<Command> getCommands();

    void setPosition(Component component) throws NoSuchInterfaceException;
    ExecutionController getExecutionController();

    void terminate();
}
