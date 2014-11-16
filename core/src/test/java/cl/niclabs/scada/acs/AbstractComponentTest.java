package cl.niclabs.scada.acs;

import cl.niclabs.scada.acs.component.controllers.monitoring.Metric;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.RecordQuerier;
import cl.niclabs.scada.acs.component.factory.ACSFactory;
import cl.niclabs.scada.acs.component.factory.exceptions.ACSFactoryException;
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
    private static String oldGcmProvider;
    private static String oldPolicy;

    @BeforeClass
    public static void setUp() throws ACSFactoryException, NoSuchInterfaceException, IOException {

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
        public void foo();
    }

    public static class FooComponent implements FooInterface, BindingController {
        public void foo() {
            for (int c = 10; c > 0; c--) {
                System.out.println("FooComponent.foo() -> " + c);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        public String[] listFc() { return new String[0]; }
        public Object lookupFc(String s) { return null; }
        public void bindFc(String s, Object o) {}
        public void unbindFc(String s) {}
    }

    public static class FooMetric extends Metric<String> {
        public String getValue() {
            return "foo";
        }
        public String calculate(RecordQuerier recordQuerier) {
            return "foo";
        }
    }
}
