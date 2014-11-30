package cl.niclabs.scada.acs.component.factory;

import cl.niclabs.scada.acs.AbstractComponentTest;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.RemoteMonitoringManager;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.component.type.PAComponentType;

/**
 * Created by mibanez
 */
public class ACSFactoryTest extends AbstractComponentTest {

    @Test
    public void acsExtraNfInterfacesOnHost() {

        ComponentType componentType = null;
        try {
            InterfaceType[] interfaceTypes = new InterfaceType[]{
                    factory.createInterfaceType("server-itf", FooInterface.class, false, false),
                    factory.createInterfaceType("client-itf", FooInterface.class, true, true)
            };
            componentType = factory.createComponentType(interfaceTypes);
        } catch (ACSFactoryException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        Assert.assertTrue(componentType instanceof PAComponentType);
        PAComponentType paComponentType = (PAComponentType) componentType;

        try {
            Assert.assertNotNull(paComponentType.getNfFcInterfaceType("client-itf" + RemoteMonitoringManager.REMOTE_MONITORING_SUFFIX));
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
            Assert.fail("interface \"client-itf\" does not have its nf acs interface");
        }

        try {
            Assert.assertNotNull(paComponentType.getNfFcInterfaceType("server-itf" +RemoteMonitoringManager.REMOTE_MONITORING_SUFFIX));
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
            Assert.fail("interface \"server-itf\" does not have its nf acs interface");
        }
    }
}
