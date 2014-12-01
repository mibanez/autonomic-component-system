package cl.niclabs.scada.acs.gcmscript.console;


import cl.niclabs.scada.acs.component.ACSManager;
import cl.niclabs.scada.acs.component.controllers.execution.ExecutionController;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import jline.console.ConsoleReader;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.identity.PAComponent;

import javax.naming.NamingException;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

public class ACSConsole implements Console, Runnable {

    // Prompt
    private String PROMPT = "GCMScript>";

    // Loaded commands: name -> implementation
    private HashMap<String, Command> commands = new HashMap<>();

    // Communication with the component through the executor controller
    private ExecutionController executorController;

    // Communication with the user through JLine library
    private ConsoleReader reader;

    private boolean finished = true;


    public class ExecCommand implements Command {

        @Override
        public String getName() {
            return "exec";
        }

        @Override
        public String getInfo() {
            return "Executes a GCMScript command";
        }

        @Override
        public Object execute(Console console, String args) throws CommandException {
            Wrapper<Serializable> result = console.getExecutionController().execute(args);
            if (result.isValid()) {
                Object value = result.unwrap();
                console.printMessage(stringValueOf(value));
                return value;
            } else {
                console.printError("Can't resolve: " + result.getMessage());
                return null;
            }
        }

        public String stringValueOf(Object value) {
            if (value instanceof PAComponent) {
                return "<Component: " + ((PAComponent) value).getComponentParameters().getName() + ">";
            } else if (value instanceof Interface) {
                return "<Interface: " + ((Interface) value).getFcItfName() + ">";
            } else if (value instanceof Set) {
                String set = "[";
                for (Object subValue : (Set) value) {
                    set += stringValueOf(subValue) + ", ";
                }
                return set.equals("[") ? "[]" : set.substring(0, set.lastIndexOf(", ")) + "]";
            } else if (value != null) {
                return value.toString();
            }
            return "";
        }
    }

    /**
     * Initialize the console on the desired autonomic component
     *
     * @param component
     * @throws IOException
     */
    public ACSConsole(Component component) throws IOException, NoSuchInterfaceException {

        reader = new ConsoleReader();
        setPosition(component);

        Command exec = new ExecCommand();
        commands.put(exec.getName(), exec);

        registerCommands();
        finished = false;
    }

    protected void registerCommands() {
        registerCommand(new HelpCommand());
        registerCommand(new ExitCommand());
        registerCommand(new ChangeCommand());
    }

    protected void registerCommand(Command command) {
        commands.put(command.getName(), command);
    }


    @Override
    public void printError(String error) {
        printMessage("Error: " + error);
    }

    @Override
    public void printMessage(String msg) {
        try {
            reader.putString(msg);
            reader.println();
        } catch (IOException e) {
            System.err.print("IO error in the console: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void printColumns(Collection<String> items) {
        try {
            reader.printColumns(items);
            reader.println();
        } catch (IOException e) {
            System.err.print("IO error in the console: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public Object execute(String commandName, String arg) {
        if (commands.containsKey(commandName)) {
            return execute(commands.get(commandName), arg);
        }
        printError(commandName + " command not found");
        return null;
    }

    @Override
    public Object execute(Command command, String arg) {
        try {
            return command.execute(this, arg);
        } catch (CommandException e) {
            printError(command.getName() + "execution failed: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Collection<Command> getCommands() {
        return Collections.unmodifiableCollection(commands.values());
    }

    @Override
    public void setPosition(Component component) throws NoSuchInterfaceException {
        ExecutionController executionCtrl = ACSManager.getExecutionController(component);
        executionCtrl.execute("true();"); // ping
        String prompt = "GCMScript@" + GCM.getNameController(component).getFcName() + "$ ";

        executorController =executionCtrl;
        PROMPT = prompt;
        reader.setPrompt(PROMPT);
    }

    @Override
    public ExecutionController getExecutionController() {
        return executorController;
    }

    @Override
    public void terminate() {
        reader.shutdown();
        finished = true;
    }

    @Override
    public void run() {
        while (!finished) {
            String input = getInput();
            processInput(input);
        }
    }

    private String getInput() {
        try {
            String input = reader.readLine(PROMPT);
            return input == null ? null : input.trim();
        } catch (IOException e) {
            printError("Can't read user input: " + e.getMessage());
        }
        return null;
    }

    private void processInput(String input) {

        if (input == null) {
            printMessage("Null input received. Exiting...");
            terminate();
        } else if (input.startsWith(":")) {
            int i = input.indexOf(' ');
            if (i != -1) {
                execute(input.substring(1, i), input.substring(i + 1));
            } else {
                execute(input.substring(1), "");
            }
        } else if (input.length() > 0) {
            execute("exec", input);
        }
    }

    public static void main(String[] args) throws Exception {

        Options options = new Options();
        options.addOption("c", true, "Init console on component ");
        options.addOption("l", false, "List RMI urls");
        options.addOption("h", true, "Host (for listing RMI urls)");
        options.addOption("p", true, "Port (for listing RMI urls)");

        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse( options, args);

        String HOST = "localhost";
        int PORT = 1099;

        if (cmd.hasOption("h")) {
            HOST = cmd.getOptionValue("h");
        }

        if(cmd.hasOption("p")) {
            PORT = Integer.parseInt(cmd.getOptionValue("p"));
        }

        if (cmd.hasOption("l")) {
            System.out.println("================ SEARCHING REGISTRY ================");
            Registry registry = LocateRegistry.getRegistry(HOST, PORT);
            String[] boundNames = registry.list();
            for (String name : boundNames) {
                System.out.println(name);
            }
            System.out.println("======================= END ========================");
        }

        if (cmd.hasOption("c")) {
            String url = cmd.getOptionValue("c");
            try {
                System.out.println("Searching component...");
                Component component = Fractive.lookup(url);
                String name = ((PAComponent) component).getComponentParameters().getName();
                System.out.println("\"" + name + "\" component found!");
                System.out.println("Starting console on ["+name+"]...");
                (new ACSConsole(component)).run();
            } catch (NamingException ne) {
                System.err.println("No component found.");
            }
        }
    }
}
