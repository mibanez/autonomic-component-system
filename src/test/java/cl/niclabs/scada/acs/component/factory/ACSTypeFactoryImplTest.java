package cl.niclabs.scada.acs.component.factory;

import cl.niclabs.scada.acs.component.ACSFractive;
import org.etsi.uri.gcm.util.GCM;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.type.PAComponentType;

import java.io.IOException;

/**
 * Created by mibanez
 */
public class ACSTypeFactoryImplTest {

    private static ACSTypeFactory typeFactory;
    private static PAGenericFactory genericFactory;
    private static String oldGcmProvider;
    private static String oldPolicy;

    @BeforeClass
    public static void setUp() throws org.objectweb.fractal.api.factory.InstantiationException, NoSuchInterfaceException, IOException {

        oldPolicy = System.getProperty("java.security.policy");
        System.setProperty("java.security.policy", ACSTypeFactoryImplTest.class.getResource("/proactive.java.policy").getPath());
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

    @Test
    public void hostComponentACSInterfacesWithFactory() {
        ComponentType componentType = null;

        try {
            InterfaceType[] interfaceTypes = new InterfaceType[]{
                    typeFactory.createGCMItfType("server-itf", FooInterface.class.getName(), false, false, "singleton"),
                    typeFactory.createGCMItfType("client-itf", FooInterface.class.getName(), true, true, "singleton")
            };
            componentType = typeFactory.createFcType(interfaceTypes);
        } catch (org.objectweb.fractal.api.factory.InstantiationException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        Assert.assertTrue(componentType instanceof PAComponentType);
        PAComponentType paComponentType = (PAComponentType) componentType;

        try {
            Assert.assertNotNull(paComponentType.getNfFcInterfaceType("client-itf-external-monitor-controller"));
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
            Assert.fail("interface \"client-itf\" does not have its nf acs interface");
        }

        try {
            Assert.assertNotNull(paComponentType.getNfFcInterfaceType("server-itf-internal-monitor-controller"));
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
            Assert.fail("interface \"server-itf\" does not have its nf acs interface");
        }

        try {
            Assert.assertNotNull(paComponentType.getNfFcInterfaceType("internal-server-monitor-controller"));
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
            Assert.fail("the nf acs interface \"internal-server\" does not exist");
        }
    }

    public static interface FooInterface {
        public int fooMethod(String fooParameter);
    }

    public static class FooComponent implements FooInterface, BindingController {
        public int fooMethod(String fooParameter) { return 0; }
        public String[] listFc() { return new String[0]; }
        public Object lookupFc(String s) throws NoSuchInterfaceException { return null; }
        public void bindFc(String s, Object o) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {}
        public void unbindFc(String s) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {}
    }

}
