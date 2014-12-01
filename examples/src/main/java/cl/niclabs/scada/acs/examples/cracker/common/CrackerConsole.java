package cl.niclabs.scada.acs.examples.cracker.common;


import cl.niclabs.scada.acs.gcmscript.console.ACSConsole;
import org.objectweb.proactive.core.component.Fractive;

import java.util.Scanner;

public class CrackerConsole {

    public static void main(String[] args) throws Exception {
        System.setProperty("gcm.provider", Fractive.class.getName());
        System.setProperty("jline.terminal", "jline.UnsupportedTerminal");

        System.out.print("Cracker component: ");
        String id = (new Scanner(System.in)).next();
        ACSConsole.main(new String[] {"-c", id});
    }
}
