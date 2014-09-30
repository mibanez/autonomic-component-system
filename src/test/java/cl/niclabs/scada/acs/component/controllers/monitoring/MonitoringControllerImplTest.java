package cl.niclabs.scada.acs.component.controllers.monitoring;

import cl.niclabs.scada.acs.component.controllers.monitoring.records.RecordStore;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.component.controllers.utils.WrongValueException;
import org.junit.Test;
import org.objectweb.fractal.api.NoSuchInterfaceException;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

/**
 * Test for MonitorControllerImpl
 *
 */
public class MonitoringControllerImplTest {

    private static class FakeMetric implements Serializable { }
    public static class FooMetric extends Metric<String> {
        private int counter = 0;
        public FooMetric() { subscribeTo(ACSEventType.VOID_REQUEST_SENT); }
        public void measure(RecordStore store) { counter++; }
        public String getValue() { return "foo-" + counter; }
    }
    public static class FooMetricEventListener implements MetricEventListener {
        public String value, name, clazzName;
        public void notifyUpdate(MetricEvent event) {
            name = event.getMetricName();
            value = event.getMetricValue().toString();
            clazzName = event.getMetricClass().getName();
        }
    }

    @Test
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public void metrics() {

        MonitoringControllerImpl monitorController = new MonitoringControllerImpl();

        try {
            MetricEventListener metricEventListener = mock(MetricEventListener.class);
            doNothing().when(metricEventListener).notifyUpdate(any(MetricEvent.class));
            monitorController.bindFc(MonitoringControllerImpl.METRIC_EVENT_LISTENER_CLIENT_ITF, metricEventListener);
        }
        catch (NoSuchInterfaceException e) {
            fail("Fail when creating the MonitorControllerImpl: " + e.getMessage());
        }

        try {
            // wrong class path
            Wrapper<Boolean> wrapper = monitorController.add("foo", "not.a.real.path.metric");
            assertEquals(false, wrapper.unwrap());
            fail("WrongValueException excepted");
        } catch (WrongValueException ignored) {}
        try {
            // class is not a metric
            Wrapper<Boolean> wrapper = monitorController.add("foo", FakeMetric.class.getName());
            assertEquals(false, wrapper.unwrap());
            fail("WrongValueException excepted");
        } catch (WrongValueException ignored) {}

        try {
            // a correct one
            Wrapper<Boolean> wrapper = monitorController.add("foo", FooMetric.class.getName());
            assertTrue(wrapper.unwrap());
            assertTrue(monitorController.getValue("foo").unwrap().equals("foo-0"));
            assertTrue(monitorController.measure("foo").unwrap().equals("foo-1"));
            assertTrue(monitorController.measure("foo").unwrap().equals("foo-2"));
            assertTrue(monitorController.measure("foo").unwrap().equals("foo-3"));
            assertTrue(monitorController.getValue("foo").unwrap().equals("foo-3"));

            // bad value cast
            try {
                Wrapper<Integer> fake = monitorController.getValue("foo");
                @SuppressWarnings("UnusedAssignment") int x = fake.unwrap();
                fail("ClassCastException expected");
            } catch (ClassCastException ignored) {
            }

            // add multiple metrics
            assertTrue(monitorController.add("foo2", FooMetric.class).unwrap());
            assertTrue(monitorController.add("foo3", FooMetric.class).unwrap());

            HashSet<String> nameSet = new HashSet<>();
            Collections.addAll(nameSet, monitorController.getRegisteredIds().unwrap());
            assertTrue(nameSet.contains("foo"));
            assertTrue(nameSet.contains("foo2"));
            assertTrue(nameSet.contains("foo3"));

            // removing a metric
            assertTrue(monitorController.remove("foo2").unwrap());

            nameSet.clear();
            Collections.addAll(nameSet, monitorController.getRegisteredIds().unwrap());
            assertTrue(nameSet.contains("foo"));
            assertFalse(nameSet.contains("foo2"));
            assertTrue(nameSet.contains("foo3"));
        } catch (WrongValueException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void subscriptions() {

        MonitoringControllerImpl monitorController = new MonitoringControllerImpl();
        FooMetricEventListener metricEventListener = new FooMetricEventListener();

        try {
            monitorController.bindFc(MonitoringControllerImpl.METRIC_EVENT_LISTENER_CLIENT_ITF, metricEventListener);
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
            fail();
        }

        try {
            monitorController.add("foo", FooMetric.class);
            assertEquals("foo-0", monitorController.getValue("foo").unwrap());

            monitorController.notifyACSEvent(ACSEventType.VOID_REQUEST_SENT);
            monitorController.notifyACSEvent(ACSEventType.VOID_REQUEST_SENT);
            monitorController.notifyACSEvent(ACSEventType.VOID_REQUEST_SENT);

            assertEquals("foo-3", monitorController.getValue("foo").unwrap());
            assertEquals("foo", metricEventListener.name);
            assertEquals("foo-3", metricEventListener.value);
            assertEquals(FooMetric.class.getName(), metricEventListener.clazzName);
        } catch (WrongValueException e) {
            fail(e.getMessage());
        }
    }

}
