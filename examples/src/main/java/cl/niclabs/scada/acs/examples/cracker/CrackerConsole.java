package cl.niclabs.scada.acs.examples.cracker;


import cl.niclabs.scada.acs.gcmscript.console.ACSConsole;
import org.objectweb.proactive.core.component.Fractive;

import java.util.Scanner;

public class CrackerConsole {

    public static void main(String[] args) throws Exception {
        System.setProperty("gcm.provider", Fractive.class.getName());
        System.setProperty("jline.terminal", "jline.UnsupportedTerminal");
        if (System.getProperty("java.security.policy") == null) {
            String securityPolicy = AbstractApp.class.getResource("/proactive.java.policy").getPath();
            System.setProperty("java.security.policy", securityPolicy);
        }

        System.out.print("Cracker component: ");
        String id = (new Scanner(System.in)).next();
        ACSConsole.main(new String[] {"-c", id});
    }
}
