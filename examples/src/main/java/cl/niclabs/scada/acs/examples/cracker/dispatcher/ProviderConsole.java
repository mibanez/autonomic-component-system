package cl.niclabs.scada.acs.examples.cracker.dispatcher;


import cl.niclabs.scada.acs.gcmscript.console.ACSConsole;
import org.objectweb.proactive.core.component.Fractive;

import java.util.Scanner;

public class ProviderConsole {

    public static void main(String[] args) throws Exception {
        System.setProperty("gcm.provider", Fractive.class.getName());
        System.setProperty("java.security.policy", Main.class.getResource("/proactive.java.policy").getPath());
        System.setProperty("jline.terminal", "jline.UnsupportedTerminal");

        String id = (new Scanner(System.in)).next();
        ACSConsole.main(new String[] {"-c", id});
    }
}
