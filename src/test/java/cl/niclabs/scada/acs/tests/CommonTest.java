package cl.niclabs.scada.acs.tests;

import cl.niclabs.scada.acs.component.ACSFractive;
import cl.niclabs.scada.acs.component.factory.ACSTypeFactory;
import org.etsi.uri.gcm.util.GCM;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.factory.*;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;

import java.io.IOException;

/**
 * Created by mibanez on 08-09-14.
 */
public class CommonTest {

    protected static ACSTypeFactory typeFactory;
    protected static PAGenericFactory genericFactory;
    private static String oldGcmProvider;
    private static String oldPolicy;

    @BeforeClass
    public static void setUp() throws org.objectweb.fractal.api.factory.InstantiationException, NoSuchInterfaceException, IOException {

        oldPolicy = System.getProperty("java.security.policy");
        System.setProperty("java.security.policy", CommonTest.class.getResource("/proactive.java.policy").getPath());
        System.setSecurityManager(new SecurityManager());

        oldGcmProvider = System.getProperty("gcm.provider");
        System.setProperty("gcm.provider", ACSFractive.class.getName());

        Component boot = GCM.getBootstrapComponent();
        typeFactory = (ACSTypeFactory) GCM.getTypeFactory(boot);
        genericFactory = (PAGenericFactory) GCM.getGenericFactory(boot);
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

}
