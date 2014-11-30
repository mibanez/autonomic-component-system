package cl.niclabs.scada.acs.gcmscript.console;

import java.util.Arrays;

public class HelpCommand implements Command {

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getInfo() {
        return "Shows this table";
    }

    @Override
    public Object execute(Console console, String args) throws CommandException {

        String msg = "This is a console to execute GCMScript commands directly over the system.\n";
        msg += "The list of available commands are listed below:\n";
        console.printMessage(msg);

        console.printColumns(Arrays.asList(new String[] { "COMMAND", "INFO" }));
        for (Command command : console.getCommands()) {
            console.printColumns(Arrays.asList(new String[] { command.getName(), command.getInfo() }));
        }

        console.printMessage("");
        return null;
    }
}
