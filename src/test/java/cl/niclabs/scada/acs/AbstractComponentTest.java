package cl.niclabs.scada.acs;

import cl.niclabs.scada.acs.component.factory.ACSFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.proactive.core.component.Fractive;

import java.io.IOException;

/**
 * Created by mibanez
 */
public abstract class AbstractComponentTest {

    protected static ACSFactory factory;
    protected static String oldGcmProvider;
    protected static String oldPolicy;

    @BeforeClass
    public static void setUp() throws org.objectweb.fractal.api.factory.InstantiationException, NoSuchInterfaceException, IOException {

        oldPolicy = System.getProperty("java.security.policy");
        System.setProperty("java.security.policy", AbstractComponentTest.class.getResource("/proactive.java.policy").getPath());
        System.setSecurityManager(new SecurityManager());

        oldGcmProvider = System.getProperty("gcm.provider");
        System.setProperty("gcm.provider", Fractive.class.getName());

        factory = new ACSFactory();
    }

    @AfterClass
    public static void setDown() {
        if (oldPolicy != null) {
            System.setProperty("java.security.policy", oldPolicy);
        }
        if (oldGcmProvider != null) {
            System.setProperty("gcm.provider", oldGcmProvider);
        }
    }

    public static interface FooInterface {
        public String foo();
    }

    public static class FooComponent implements FooInterface, BindingController {
        public String foo() {
            for (int c = 5; c > 0; c--) {
                System.out.println("FooComponent.foo() -> " + c);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return "OK";
        }
        public String[] listFc() { return new String[0]; }
        public Object lookupFc(String s) { return null; }
        public void bindFc(String s, Object o) {}
        public void unbindFc(String s) {}
    }

}
