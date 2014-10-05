package cl.niclabs.scada.acs.component.controllers.monitoring;

import cl.niclabs.scada.acs.component.controllers.*;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.RecordStore;
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
        public FooMetric() {
            try {
                subscribeTo(ACSEventType.VOID_REQUEST_SENT);
            } catch (CommunicationException e) {
                e.printStackTrace();
            }
        }
        public String measure(RecordStore store) { counter++; return getValue(); }
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

        Metric fooMetric = null;
        try {
            // wrong class path
            fooMetric = monitorController.add("foo", "not.a.real.path.metric");
            fail("WrongValueException excepted");
        } catch (InvalidElementException ignored) {
        } catch (DuplicatedElementIdException e) {
            fail("Unexpected DuplicatedElementIdException: " + e.getMessage());
        }


        try {
            // class is not a metric
            fooMetric = monitorController.add("foo", FakeMetric.class.getName());
            fail("WrongValueException excepted");
        } catch (InvalidElementException ignored) {
        } catch (DuplicatedElementIdException e) {
            fail("Unexpected DuplicatedElementIdException: " + e.getMessage());
        }

        try {
            // a correct one
            fooMetric = monitorController.add("foo", FooMetric.class.getName());

            assertEquals("foo-0", fooMetric.getValue());
            assertEquals("foo-1", fooMetric.measure(null));
            assertEquals("foo-2", fooMetric.measure(null));
            assertEquals("foo-3", fooMetric.measure(null));
            assertEquals("foo-3", fooMetric.getValue());

            // bad value cast
            try {
                Wrapper<Integer> fake = monitorController.getValue("foo");
                @SuppressWarnings("UnusedAssignment") int x = fake.unwrap();
                fail("ClassCastException expected");
            } catch (ClassCastException ignored) {
            }

            // bad value cast 2
            try {
                Metric<Integer> fakeMetric = monitorController.get("foo");
                Integer fakeValue = fakeMetric.getValue();
                fail("ClassCastException expected");
            } catch (ClassCastException ignored) {
            }

            // add multiple metrics
            monitorController.add("foo2", FooMetric.class);
            monitorController.add("foo3", FooMetric.class);

            HashSet<String> nameSet = new HashSet<>();
            Collections.addAll(nameSet, monitorController.getRegisteredIds());
            assertTrue(nameSet.contains("foo"));
            assertTrue(nameSet.contains("foo2"));
            assertTrue(nameSet.contains("foo3"));

            // removing a metric
            monitorController.remove("foo2");

            nameSet.clear();
            Collections.addAll(nameSet, monitorController.getRegisteredIds());
            assertTrue(nameSet.contains("foo"));
            assertFalse(nameSet.contains("foo2"));
            assertTrue(nameSet.contains("foo3"));
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

}
