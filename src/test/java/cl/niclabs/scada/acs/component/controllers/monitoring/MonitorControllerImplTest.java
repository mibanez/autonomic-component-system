package cl.niclabs.scada.acs.component.controllers.monitoring;

import cl.niclabs.scada.acs.component.controllers.MonitorController;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.*;

/**
 * Test for MonitorControllerImpl
 *
 */
public class MonitorControllerImplTest {

    @Test
    public void metrics() {

        class FooMetric extends Metric<String> {

            private int counter = 0;

            @Override
            public void measure() {
                counter++;
            }

            @Override
            public String getValue() {
                return "foo-" + counter;
            }
        }

        MonitorController monitorController = new MonitorControllerImpl();
        assertTrue(monitorController.addMetric("foo", new FooMetric()).unwrap());
        assertTrue(monitorController.getValue("foo").unwrap().equals("foo-0"));
        assertTrue(monitorController.measure("foo").unwrap().equals("foo-1"));
        assertTrue(monitorController.measure("foo").unwrap().equals("foo-2"));
        assertTrue(monitorController.measure("foo").unwrap().equals("foo-3"));
        assertTrue(monitorController.getValue("foo").unwrap().equals("foo-3"));

        Wrapper<Integer> fake = monitorController.getValue("foo");
        try {
            @SuppressWarnings("UnusedDeclaration")
            int x = fake.unwrap();
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
            // ok
        }

        assertTrue(monitorController.addMetric("foo2", new FooMetric()).unwrap());
        assertTrue(monitorController.addMetric("foo3", new FooMetric()).unwrap());

        HashSet<String> nameSet = new HashSet<>();
        Collections.addAll(nameSet, monitorController.getMetricNames().unwrap());

        assertTrue(nameSet.contains("foo"));
        assertTrue(nameSet.contains("foo2"));
        assertTrue(nameSet.contains("foo3"));

        assertTrue(monitorController.removeMetric("foo2").unwrap());

        nameSet.clear();
        Collections.addAll(nameSet, monitorController.getMetricNames().unwrap());

        assertTrue(nameSet.contains("foo"));
        assertFalse(nameSet.contains("foo2"));
        assertTrue(nameSet.contains("foo3"));
    }

}
