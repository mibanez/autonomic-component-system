package cl.niclabs.scada.acs.examples.cracker;

import cl.niclabs.scada.acs.component.adl.ACSAdlFactory;
import cl.niclabs.scada.acs.component.adl.ACSAdlFactoryFactory;
import cl.niclabs.scada.acs.examples.cracker.component.Client;
import cl.niclabs.scada.acs.examples.cracker.component.Cracker;
import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.Utils;

import javax.naming.NamingException;
import java.io.IOException;
import java.util.Scanner;


public class ClientApp {

    private static final String CLIENT_ADL = "cl.niclabs.scada.acs.examples.cracker.component.Client";

    public static void main(String[] args) {

        System.setProperty("gcm.provider", Fractive.class.getName());
        System.setProperty("java.security.policy", ClientApp.class.getResource("/proactive.java.policy").getPath());

        Component crackerComp = null;

        Scanner scanner = new Scanner(System.in);
        System.out.print("Cracker Component URL: ");
        try {
            crackerComp = Fractive.lookup(scanner.nextLine().trim());
        } catch (IOException | NamingException e) {
            System.err.println("Cracker component couldn't be found: " + e.getMessage());
            System.exit(1);
        }

        int maxLength = askMaxLengthParameter(scanner);
        int nOfTest = askNOfTestParameter(scanner);
        long delay = askDelayParameter(scanner);

        try {
            ACSAdlFactory adlFactory = ACSAdlFactoryFactory.getACSAdlFactory();
            Component clientComp = adlFactory.newACSComponent(CLIENT_ADL, null);
            Client client = (Client) clientComp.getFcInterface(Client.NAME);

            Utils.getPABindingController(clientComp).bindFc(Cracker.NAME, crackerComp.getFcInterface(Cracker.NAME));
            Utils.getPAGCMLifeCycleController(clientComp).startFc();

            client.start(maxLength, nOfTest, delay);
            while (true) {
                Thread.sleep(Long.MAX_VALUE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Internal error: " + e.getMessage());
            System.exit(2);
        }
    }

    private static int askMaxLengthParameter(Scanner scanner) {
        int MAX_LENGTH = CrackerConfig.DEFAULT_MAX_LENGTH;
        System.out.print("Passwords length [" + MAX_LENGTH + "]: ");
        String next = scanner.nextLine().trim();
        try {
            MAX_LENGTH = Integer.parseInt(next);
        } catch (NumberFormatException e) {
            if (!next.equals("")) {
                System.err.println("Invalid value, using default.");
            }
        }
        return MAX_LENGTH;
    }

    private static int askNOfTestParameter(Scanner scanner) {
        int N_OF_TEST = CrackerConfig.DEFAULT_N_OF_TEST;
        System.out.print("Number of Test [" + N_OF_TEST + "]: ");
        String next = scanner.nextLine().trim();
        try {
            N_OF_TEST = Integer.parseInt(next);
        } catch (NumberFormatException e) {
            if (!next.equals("")) {
                System.err.println("Invalid value, using default.");
            }
        }
        return N_OF_TEST;
    }

    private static long askDelayParameter(Scanner scanner) {
        long DELAY = CrackerConfig.DEFAULT_DELAY;
        System.out.print("Delay in millis [" + DELAY + "]: ");
        String next = scanner.nextLine().trim();
        try {
            DELAY = Long.parseLong(next);
        } catch (NumberFormatException e) {
            if (!next.equals("")) {
                System.err.println("Invalid value, using default.");
            }
        }
        return DELAY;
    }

}
