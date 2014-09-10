package cl.niclabs.scada.acs.component.utils;

import cl.niclabs.scada.acs.component.ACSFractive;
import cl.niclabs.scada.acs.component.controllers.MonitorController;
import cl.niclabs.scada.acs.component.controllers.MonitorControllerMulticast;
import cl.niclabs.scada.acs.component.controllers.exceptions.ACSIntegrationException;
import cl.niclabs.scada.acs.component.factory.ACSTypeFactory;
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
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

/**
 * Created by mibanez on 08-09-14.
 */
public class ACSBuildHelperTest {

    private static ACSTypeFactory typeFactory;
    private static PAGenericFactory genericFactory;
    private static String oldGcmProvider;
    private static String oldPolicy;

    @BeforeClass
    public static void setUp() throws org.objectweb.fractal.api.factory.InstantiationException, NoSuchInterfaceException, IOException {

        oldPolicy = System.getProperty("java.security.policy");
        System.setProperty("java.security.policy", ACSBuildHelperTest.class.getResource("/proactive.java.policy").getPath());
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
    public void getAcsNfInterfaces() {
        InterfaceType[] interfaceTypes = null;
        try {
            interfaceTypes = new InterfaceType[]{
                    typeFactory.createGCMItfType("server-itf", FooInterface.class.getName(), false, false, "singleton"),
                    typeFactory.createGCMItfType("client-itf", FooInterface.class.getName(), true, true, "singleton")
            };
        } catch (InstantiationException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        List<InterfaceType> acsNfInterfaces = null;
        try {
            acsNfInterfaces = ACSBuildHelper.getAcsNfInterfaces(typeFactory, interfaceTypes);
        } catch (InstantiationException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        HashSet<String> set = new HashSet<String>();
        for (InterfaceType itfType : acsNfInterfaces) {
            set.add(itfType.getFcItfName());
            boolean isMon = itfType.getFcItfSignature().equals(MonitorController.class.getName());
            boolean isMonMulticast = itfType.getFcItfSignature().equals(MonitorControllerMulticast.class.getName());
            Assert.assertTrue(isMon || isMonMulticast);
        }

        Assert.assertTrue(set.contains("client-itf-external-monitor-controller"));
        Assert.assertTrue(set.contains("server-itf-internal-monitor-controller"));
        Assert.assertTrue(set.contains("internal-server-monitor-controller"));
    }

    @Test
    public void addACSControllers() {
        Component foo = null;
        try {
            ComponentType componentType = typeFactory.createFcType(new InterfaceType[]{
                    typeFactory.createGCMItfType("server-itf", FooInterface.class.getName(), false, false, "singleton"),
                    typeFactory.createGCMItfType("client-itf", FooInterface.class.getName(), true, true, "singleton")
            });
            foo = genericFactory.newFcInstance(componentType,
                    new ControllerDescription("FooComponent", "primitive"),
                    new ContentDescription(FooComponent.class.getName()), null);
        } catch (InstantiationException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        try {
            ACSBuildHelper.addObjectControllers(foo);
        } catch (ACSIntegrationException e) {
            e.printStackTrace();
            Assert.fail("addObjectControllers: " + e.getMessage());
        }

        try {
            ACSBuildHelper.addACSControllers(typeFactory, genericFactory, foo);
        } catch (ACSIntegrationException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        Object monitorObj = null;
        try {
            monitorObj = foo.getFcInterface("monitor-controller");
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        Assert.assertTrue(monitorObj instanceof MonitorController);
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
