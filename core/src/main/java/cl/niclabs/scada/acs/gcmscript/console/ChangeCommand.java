package cl.niclabs.scada.acs.gcmscript.console;

import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.component.identity.PAComponent;

import java.util.Set;

public class ChangeCommand implements Command {

    @Override
    public String getName() {
        return "cc";
    }

    @Override
    public String getInfo() {
        return "Change the current terminal component position";
    }

    @Override
    public Object execute(Console console, String args) throws CommandException {
        Wrapper<PAComponent> result = console.getExecutionController().execute(args);
        if (result.isValid()) {
            try {
                Object value = result.unwrap();
                if (value instanceof PAComponent) {
                    console.setPosition((PAComponent) value);
                    return value;
                } else if (value instanceof Set && ((Set<?>) value).size() >= 0) {
                    Object element = ((Set<?>) value).toArray()[0];
                    if (element instanceof PAComponent) {
                        console.setPosition((PAComponent) element);
                        return element;
                    }
                }
            } catch (NoSuchInterfaceException e) {
                console.printError("Can't change position (NoSuchInterfaceException: " + e.getMessage() + ")");
            }
            console.printError("The argument must be a component");
        } else {
            console.printError("Can't resolve: " + result.getMessage());
        }
        return null;
    }
}
