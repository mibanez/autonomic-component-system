package cl.niclabs.scada.acs.multiactivity.component;

import cl.niclabs.scada.acs.AbstractComponentTest;
import cl.niclabs.scada.acs.component.ACSUtils;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.Metric;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.MetricStore;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.RecordStore;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.component.Utils;

/**
 * A copy of ComponentMultiActiveService, using the custom ACS extensions.
 * 
 */
public class ACSMultiActiveServiceTest extends AbstractComponentTest {

    public static class FooMetric extends Metric<String> {
        public String calculate(RecordStore recordStore, MetricStore metricStore) {
            return getValue();
        }
        public String getValue() { return "Foo Foo Foo !!"; }
    }

    @Test
    public void multiActiveServiceTest() {
        try {
            ComponentType componentType = factory.createComponentType(new InterfaceType[]{
                    factory.createInterfaceType("server-itf", FooInterface.class.getName(), false, false, "singleton"),
                    factory.createInterfaceType("client-itf", FooInterface.class.getName(), true, true, "singleton")
            });
            Component foo = factory.createPrimitiveComponent("FooComponent", componentType, FooComponent.class, null);

            Utils.getPAMembraneController(foo).startMembrane();
            Utils.getPAGCMLifeCycleController(foo).startFc();

            FooInterface fooItf = (FooInterface) foo.getFcInterface("server-itf");
            MonitoringController monitoringController = ACSUtils.getMonitoringController(foo);
            monitoringController.add("foo-metric", FooMetric.class);

            fooItf.foo();

            for (int i = 0; i < 10; i++) {
                System.out.println(monitoringController.getValue("foo-metric").unwrap());
                Thread.sleep(100);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

}
