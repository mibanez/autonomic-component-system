package cl.niclabs.scada.acs.multiactivity.component;

import cl.niclabs.scada.acs.AbstractComponentTest;
import cl.niclabs.scada.acs.component.controllers.MonitorController;
import cl.niclabs.scada.acs.component.controllers.monitoring.Metric;
import cl.niclabs.scada.acs.component.factory.exceptions.ACSFactoryException;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.component.Utils;

/**
 * A copy of ComponentMultiActiveService, using the custom ACS extensions.
 * 
 */
public class ACSMultiActiveServiceTest extends AbstractComponentTest {

    @Test
    public void multiActiveServiceTest() {

        Component foo = null;
        try {
            ComponentType componentType = factory.createComponentType(new InterfaceType[]{
                    factory.createInterfaceType("server-itf", FooInterface.class.getName(), false, false, "singleton"),
                    factory.createInterfaceType("client-itf", FooInterface.class.getName(), true, true, "singleton")
            });
            foo = factory.createComponent("FooComponent", FooComponent.class, componentType, "primitive", null);
        } catch (ACSFactoryException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        try {
            try {
                Utils.getPAMembraneController(foo).startMembrane();
                Utils.getPAGCMLifeCycleController(foo).startFc();
            } catch (IllegalLifeCycleException e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }

            FooInterface fooItf = (FooInterface) foo.getFcInterface("server-itf");
            MonitorController monitorController = (MonitorController) foo.getFcInterface("monitor-controller");
            monitorController.addMetric("foo-metric", new FooMetric());
            fooItf.foo();

            try {
                for (int i = 0; i < 10; i++) {
                    System.out.println(monitorController.getValue("foo-metric").getMessage());
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    public static class FooMetric extends Metric<String> {

        @Override
        public void measure() {
            // nothing
        }

        @Override
        public String getValue() {
            return "Foo Foo Foo !!";
        }

    }
}
