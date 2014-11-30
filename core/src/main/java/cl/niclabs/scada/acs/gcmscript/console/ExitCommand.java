package cl.niclabs.scada.acs.gcmscript.console;


public class ExitCommand implements Command {
    @Override
    public String getName() {
        return "exit";
    }

    @Override
    public String getInfo() {
        return "Exits the console";
    }

    @Override
    public Object execute(Console console, String args) throws CommandException {
        console.printMessage("Bye!");
        console.terminate();
        return null;
    }
}
