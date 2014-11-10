package cl.niclabs.scada.acs.component.controllers.monitoring;

import cl.niclabs.scada.acs.component.ACSUtils;
import cl.niclabs.scada.acs.component.controllers.DuplicatedElementIdException;
import cl.niclabs.scada.acs.component.controllers.InvalidElementException;
import cl.niclabs.scada.acs.component.controllers.MetricProxy;
import cl.niclabs.scada.acs.component.controllers.Wrapper;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.RecordStore;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.component.identity.PAComponent;

import java.io.Serializable;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
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
            subscribeTo(RecordEvent.VOID_REQUEST_SENT);
        }
        public String calculate(RecordStore store) { counter++; return getValue(); }
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
    public void metrics() throws NoSuchInterfaceException {

        MonitoringControllerImpl monitorController = new MonitoringControllerImpl();
        Component host = mock(PAComponent.class);
        doReturn(monitorController).when(host).getFcInterface(eq(ACSUtils.MONITORING_CONTROLLER));
        monitorController.setHostComponent(host);

        try {
            MetricEventListener metricEventListener = Mockito.mock(MetricEventListener.class);
            Mockito.doNothing().when(metricEventListener).notifyUpdate(Matchers.any(MetricEvent.class));
            monitorController.bindFc(MonitoringControllerImpl.METRIC_EVENT_LISTENER_CLIENT_ITF, metricEventListener);
        }
        catch (NoSuchInterfaceException e) {
            Assert.fail("Fail when creating the MonitorControllerImpl: " + e.getMessage());
        }

        MetricProxy fooMetric = null;
        try {
            // wrong class path
            fooMetric = monitorController.add("foo", "not.a.real.path.metric");
            Assert.fail("WrongValueException excepted");
        } catch (InvalidElementException ignored) {
        } catch (DuplicatedElementIdException e) {
            Assert.fail("Unexpected DuplicatedElementIdException: " + e.getMessage());
        }


        try {
            // class is not a metric
            fooMetric = monitorController.add("foo", FakeMetric.class.getName());
            Assert.fail("WrongValueException excepted");
        } catch (InvalidElementException ignored) {
        } catch (DuplicatedElementIdException e) {
            Assert.fail("Unexpected DuplicatedElementIdException: " + e.getMessage());
        }

        try {
            // a correct one
            fooMetric = monitorController.add("foo", FooMetric.class.getName());

            assertEquals("foo-0", fooMetric.getValue());
            assertEquals("foo-1", fooMetric.measure());
            assertEquals("foo-2", fooMetric.measure());
            assertEquals("foo-3", fooMetric.measure());
            assertEquals("foo-3", fooMetric.getValue());

            // bad value cast
            try {
                Wrapper<Integer> fake = monitorController.getValue("foo");
                @SuppressWarnings("UnusedAssignment") int x = fake.unwrap();
                Assert.fail("ClassCastException expected");
            } catch (ClassCastException ignored) {
            }

            // bad value cast 2
            try {
                MetricProxy proxy = Mockito.mock(MetricProxy.class);
                doReturn("foo").when(proxy).getValue();
                Integer fakeValue = (Integer) proxy.getValue();
                Assert.fail("ClassCastException expected");
            } catch (ClassCastException ignored) {
            }

            // add multiple metrics
            monitorController.add("foo2", FooMetric.class);
            monitorController.add("foo3", FooMetric.class);

            HashSet<String> nameSet = new HashSet<>(monitorController.getRegisteredIds());
            Assert.assertTrue(nameSet.contains("foo"));
            Assert.assertTrue(nameSet.contains("foo2"));
            Assert.assertTrue(nameSet.contains("foo3"));

            // removing a metric
            monitorController.remove("foo2");

            nameSet.clear();
            nameSet.addAll(monitorController.getRegisteredIds());
            Assert.assertTrue(nameSet.contains("foo"));
            Assert.assertFalse(nameSet.contains("foo2"));
            Assert.assertTrue(nameSet.contains("foo3"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void subscriptions() throws NoSuchInterfaceException {

        MonitoringControllerImpl monitorController = new MonitoringControllerImpl();
        FooMetricEventListener metricEventListener = new FooMetricEventListener();

        Component host = mock(PAComponent.class);
        doReturn(monitorController).when(host).getFcInterface(eq(ACSUtils.MONITORING_CONTROLLER));
        monitorController.setHostComponent(host);

        try {
            monitorController.bindFc(MonitoringControllerImpl.METRIC_EVENT_LISTENER_CLIENT_ITF, metricEventListener);
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
            Assert.fail();
        }

        try {
            monitorController.add("foo", FooMetric.class);
            assertEquals("foo-0", monitorController.getValue("foo").unwrap());

            monitorController.notifyACSEvent(RecordEvent.VOID_REQUEST_SENT);
            monitorController.notifyACSEvent(RecordEvent.VOID_REQUEST_SENT);
            monitorController.notifyACSEvent(RecordEvent.VOID_REQUEST_SENT);

            assertEquals("foo-3", monitorController.getValue("foo").unwrap());
            Assert.assertEquals("foo", metricEventListener.name);
            Assert.assertEquals("foo-3", metricEventListener.value);
            Assert.assertEquals(FooMetric.class.getName(), metricEventListener.clazzName);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

}
