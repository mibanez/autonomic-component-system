package cl.niclabs.scada.acs.component.factory;

import cl.niclabs.scada.acs.AbstractComponentTest;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringControllerImpl;
import cl.niclabs.scada.acs.component.factory.exceptions.ACSFactoryException;
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
                    factory.createInterfaceType("server-itf", FooInterface.class.getName(), false, false),
                    factory.createInterfaceType("client-itf", FooInterface.class.getName(), true, true)
            };
            componentType = factory.createComponentType(interfaceTypes);
        } catch (ACSFactoryException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        Assert.assertTrue(componentType instanceof PAComponentType);
        PAComponentType paComponentType = (PAComponentType) componentType;

        try {
            Assert.assertNotNull(paComponentType.getNfFcInterfaceType("client-itf" + MonitoringControllerImpl.EXTERNAL_MONITORING_ITF));
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
            Assert.fail("interface \"client-itf\" does not have its nf acs interface");
        }

        try {
            Assert.assertNotNull(paComponentType.getNfFcInterfaceType("server-itf" + MonitoringControllerImpl.INTERNAL_MONITORING_ITF));
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
            Assert.fail("interface \"server-itf\" does not have its nf acs interface");
        }

        try {
            Assert.assertNotNull(paComponentType.getNfFcInterfaceType(MonitoringControllerImpl.INTERNAL_SERVER_MONITORING_ITF));
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
            Assert.fail("the nf acs interface \"internal-server\" does not exist");
        }
    }

}
