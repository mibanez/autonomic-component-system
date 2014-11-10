package cl.niclabs.scada.acs.examples.optimizer;

import cl.niclabs.scada.acs.component.adl.ACSAdlFactory;
import cl.niclabs.scada.acs.component.adl.ACSAdlFactoryFactory;
import cl.niclabs.scada.acs.examples.optimizer.components.Cracker;
import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.Utils;

import java.security.MessageDigest;

/**
 * Created by mibanez
 */
public class Main {

    public static void main(String[] args) throws Exception {

        MessageDigest md5 = MessageDigest.getInstance("MD5");
        String password = getRandomWord();

        System.setProperty("gcm.provider", Fractive.class.getName());
        System.setProperty("java.security.policy", Main.class.getResource("/proactive.java.policy").getPath());

        System.out.println("Get adl factory...");
        ACSAdlFactory adlFactory = (ACSAdlFactory) ACSAdlFactoryFactory.getACSAdlFactory();

        System.out.println("Create component...");
        Component crackerComponent = (Component) adlFactory.newACSComponent("cl.niclabs.scada.acs.examples.optimizer.components.Cracker", null);

        System.out.println("Start life cycle...");
        Utils.getPAGCMLifeCycleController(crackerComponent).startFc();

        System.out.println("Send request...");
        Cracker cracker = (Cracker) crackerComponent.getFcInterface("cracker-server-itf");
        String crackedPassword = cracker.crack(md5.digest(password.getBytes())).unwrap();
        System.out.println("Original password: " + password + " crackedPassword: " + crackedPassword);
    }

    private static String getRandomWord() {
        String word = "";
        int base = OptimizerConfig.ALPHABET.length();
        for (int i = 0; i < OptimizerConfig.PASSWORD_MAX_LENGTH; i ++) {
            word += OptimizerConfig.ALPHABET.charAt((int) (Math.random() * base));
        }
        return word;
    }

}
