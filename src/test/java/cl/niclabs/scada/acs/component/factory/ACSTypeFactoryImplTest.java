package cl.niclabs.scada.acs.component.factory;

import cl.niclabs.scada.acs.AbstractComponentTest;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.component.type.PAComponentType;

/**
 * Created by mibanez
 */
public class ACSTypeFactoryImplTest extends AbstractComponentTest {

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

}
