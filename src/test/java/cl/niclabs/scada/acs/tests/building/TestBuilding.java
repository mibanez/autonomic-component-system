package cl.niclabs.scada.acs.tests.building;

import cl.niclabs.scada.acs.component.ACSFractive;
import cl.niclabs.scada.acs.component.factory.ACSTypeFactory;
import cl.niclabs.scada.acs.tests.CommonTest;
import org.etsi.uri.gcm.util.GCM;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.type.PAComponentType;
import utils.components.FooComponent;
import utils.components.FooInterface;

import java.io.IOException;

/**
 * Created by mibanez on 08-09-14.
 */
public class TestBuilding extends CommonTest {

    @Test
    public void nfAcsInterfaces() {
        ComponentType componentType = null;

        try {
            InterfaceType[] interfaceTypes = new InterfaceType[]{
                    typeFactory.createGCMItfType("server-itf", FooInterface.class.getName(), false, false, "singleton"),
                    typeFactory.createGCMItfType("client-itf", FooInterface.class.getName(), true, true, "singleton")
            };
            componentType = typeFactory.createFcType(interfaceTypes);
        } catch (InstantiationException e) {
            Assert.fail(e.getMessage());
        }

        Assert.assertTrue(componentType instanceof PAComponentType);
        PAComponentType paComponentType = (PAComponentType) componentType;

        try {
            Assert.assertNotNull(paComponentType.getNfFcInterfaceType("client-itf-external-monitor-controller"));
        } catch (NoSuchInterfaceException e) {
            Assert.fail("interface \"client-itf\" does not have its nf acs interface");
        }

        try {
            Assert.assertNotNull(paComponentType.getNfFcInterfaceType("server-itf-internal-monitor-controller"));
        } catch (NoSuchInterfaceException e) {
            Assert.fail("interface \"server-itf\" does not have its nf acs interface");
        }

        try {
            Assert.assertNotNull(paComponentType.getNfFcInterfaceType("internal-server-monitor-controller"));
        } catch (NoSuchInterfaceException e) {
            Assert.fail("the nf acs interface \"internal-server\" does not exist");
        }

        Component foo = null;
        try {
            foo = genericFactory.newFcInstance(componentType,
                    new ControllerDescription("FooComponent", "primitive"),
                    new ContentDescription(FooComponent.class.getName()), null);
        } catch (InstantiationException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

}
