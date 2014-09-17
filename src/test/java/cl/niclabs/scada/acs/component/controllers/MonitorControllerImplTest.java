package cl.niclabs.scada.acs.component.controllers;

import cl.niclabs.scada.acs.component.controllers.monitoring.Metric;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by mibanez
 */
public class MonitorControllerImplTest {

    @Test
    public void metric() {

        class FooMetric extends Metric<String> {

            @Override
            public String getValue() {
                return "foo";
            }
        }

        MonitorController monitorController = new MonitorControllerImpl();
        assertTrue(monitorController.addMetric("foo", new FooMetric()).unwrap());
        assertTrue(monitorController.getValue("foo").unwrap().equals("foo"));

        Wrapper<Integer> fake = monitorController.getValue("foo");
        try {
            //noinspection UnusedAssignment
            int x = fake.unwrap();
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
            // ok
        }
    }

}
