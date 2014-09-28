package cl.niclabs.scada.acs.component.controllers.monitor;

import cl.niclabs.scada.acs.component.controllers.monitor.records.RecordStore;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
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
public class MonitorControllerImplTest {

    private static class FakeMetric implements Serializable { }
    public static class FooMetric extends Metric<String> {
        private int counter = 0;
        public FooMetric() { subscribeTo(ACSEventType.VOID_REQUEST_SENT); }
        public void measure(RecordStore store) { counter++; }
        public String getValue() { return "foo-" + counter; }
    }

    @Test
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public void metrics() {

        MonitorControllerImpl monitorController = getSimpleMonitor();
        if (monitorController == null) {
            fail("Fail when creating the MonitorControllerImpl");
        }

        Wrapper<Boolean> wrapper = monitorController.addMetric("foo", FooMetric.class.getName());
        System.out.print("AddMetric result = " + wrapper.getMessage());
        if (wrapper.getException() != null) {
            wrapper.getException().printStackTrace();
        }
        assertTrue(wrapper.unwrap());
        assertTrue(monitorController.getValue("foo").unwrap().equals("foo-0"));
        assertTrue(monitorController.measure("foo").unwrap().equals("foo-1"));
        assertTrue(monitorController.measure("foo").unwrap().equals("foo-2"));
        assertTrue(monitorController.measure("foo").unwrap().equals("foo-3"));
        assertTrue(monitorController.getValue("foo").unwrap().equals("foo-3"));

        try {
            Wrapper<Integer> fake = monitorController.getValue("foo");
            @SuppressWarnings("UnusedAssignment") int x = fake.unwrap();
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
            // ok
        }

        assertTrue(monitorController.addMetric("foo2", FooMetric.class).unwrap());
        assertTrue(monitorController.addMetric("foo3", FooMetric.class).unwrap());

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

    @Test
    public void wrongMetricInstantiations() {

        MonitorControllerImpl monitorController = getSimpleMonitor();
        if (monitorController == null) {
            fail("Fail when creating the MonitorControllerImpl");
        }

        // wrong path
        Wrapper<Boolean> wrapper = monitorController.addMetric("foo", "not.a.real.path.metric");
        assertEquals(false, wrapper.unwrap());
        assertNotNull(wrapper.getException());

        // not a metric
        wrapper = monitorController.addMetric("foo", FakeMetric.class.getName());
        assertEquals(false, wrapper.unwrap());
        assertNull(wrapper.getException());
    }

    @Test
    public void metricSubscription() {

        MonitorControllerImpl monitorController = getSimpleMonitor();
        if (monitorController == null) {
            fail("Fail when creating the MonitorControllerImpl");
        }

        monitorController.addMetric("foo", FooMetric.class);
        assertEquals("foo-0", monitorController.getValue("foo").unwrap());

        monitorController.notifyACSEvent(ACSEventType.VOID_REQUEST_SENT);
        monitorController.notifyACSEvent(ACSEventType.VOID_REQUEST_SENT);
        monitorController.notifyACSEvent(ACSEventType.VOID_REQUEST_SENT);

        assertEquals("foo-3", monitorController.getValue("foo").unwrap());
    }

    @Test
    public void metricEventListener() {

        MonitorControllerImpl monitorController = new MonitorControllerImpl();

        try {
            MetricEventListener metricEventListener = new MetricEventListener() {
                @Override
                public void notifyUpdate(MetricEvent event) {
                    assertTrue(true);
                }
            };
            monitorController.bindFc(MonitorControllerImpl.METRIC_EVENT_LISTENER_CLIENT_ITF, metricEventListener);
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
            fail();
        }


    }

    private MonitorControllerImpl getSimpleMonitor() {
        try {
            MetricEventListener metricEventListener = mock(MetricEventListener.class);
            doNothing().when(metricEventListener).notifyUpdate(any(MetricEvent.class));

            MonitorControllerImpl monitorController = new MonitorControllerImpl();
            monitorController.bindFc(MonitorControllerImpl.METRIC_EVENT_LISTENER_CLIENT_ITF, metricEventListener);

            return monitorController;
        }
        catch (NoSuchInterfaceException e) {
            e.printStackTrace();
            return null;
        }
    }
}
