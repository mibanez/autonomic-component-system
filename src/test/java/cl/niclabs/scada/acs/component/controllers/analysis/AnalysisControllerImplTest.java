package cl.niclabs.scada.acs.component.controllers.analysis;

import cl.niclabs.scada.acs.component.controllers.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.monitoring.MetricEvent;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.component.controllers.utils.WrongValueException;
import org.junit.Test;
import org.objectweb.fractal.api.NoSuchInterfaceException;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Created by mibanez
 */
public class AnalysisControllerImplTest {

    public static class FakeRule implements Serializable {}
    public static class FooRule extends Rule {
        public int counter = -1;
        public FooRule() {
            subscribeTo("foo");
        }
        public ACSAlarm verify(MonitoringController monitoringController) {
            counter = (counter + 1) % 4;
            switch (counter) {
                case 0: return ACSAlarm.OK;
                case 1: return ACSAlarm.WARNING;
                case 2: return ACSAlarm.VIOLATION;
                case 3: return ACSAlarm.ERROR;
            }
            return ACSAlarm.OK;
        }
    }

    @Test
    public void rules() {

        AnalysisControllerImpl analysisController = new AnalysisControllerImpl();
        try {
            MonitoringController monitoringController = mock(MonitoringController.class);
            analysisController.bindFc(AnalysisControllerImpl.MONITORING_CONTROLLER_CLIENT_ITF, monitoringController);
        }
        catch (NoSuchInterfaceException e) {
            fail("Fail when creating the MonitorControllerImpl: " + e.getMessage());
        }

        try {
            // wrong class path
            Wrapper<Boolean> wrapper = analysisController.add("foo", "not.a.real.path.rule");
            assertEquals(false, wrapper.unwrap());
            fail("WrongValueException expected");
        } catch (WrongValueException ignored) {}

        try {
            // class is not a rule
            Wrapper<Boolean> wrapper = analysisController.add("foo", FakeRule.class.getName());
            assertEquals(false, wrapper.unwrap());
            fail("WrongValueException expected");
        } catch (WrongValueException ignored) {}

        try {
            // a correct one
            Wrapper<Boolean> wrapper = analysisController.add("foo", FooRule.class.getName());
            assertTrue(wrapper.unwrap());
            assertEquals(ACSAlarm.OK, analysisController.verify("foo").unwrap());
            assertEquals(ACSAlarm.WARNING, analysisController.verify("foo").unwrap());
            assertEquals(ACSAlarm.VIOLATION, analysisController.verify("foo").unwrap());
            assertEquals(ACSAlarm.ERROR, analysisController.verify("foo").unwrap());

            // add multiple rules
            assertTrue(analysisController.add("foo2", FooRule.class).unwrap());
            assertTrue(analysisController.add("foo3", FooRule.class).unwrap());

            HashSet<String> nameSet = new HashSet<>();
            Collections.addAll(nameSet, analysisController.getRegisteredIds().unwrap());
            assertTrue(nameSet.contains("foo"));
            assertTrue(nameSet.contains("foo2"));
            assertTrue(nameSet.contains("foo3"));

            // removing a rule
            assertTrue(analysisController.remove("foo2").unwrap());

            nameSet.clear();
            Collections.addAll(nameSet, analysisController.getRegisteredIds().unwrap());
            assertTrue(nameSet.contains("foo"));
            assertFalse(nameSet.contains("foo2"));
            assertTrue(nameSet.contains("foo3"));
        } catch (WrongValueException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void subscriptions() {

        AnalysisControllerImpl analysisController = new AnalysisControllerImpl();
        try {
            MonitoringController monitoringController = mock(MonitoringController.class);
            analysisController.bindFc(AnalysisControllerImpl.MONITORING_CONTROLLER_CLIENT_ITF, monitoringController);
        }
        catch (NoSuchInterfaceException e) {
            fail("Fail when creating the MonitorControllerImpl: " + e.getMessage());
        }

        analysisController.add("foo", FooRule.class.getName());

        MetricEvent metricEvent = mock(MetricEvent.class);
        analysisController.notifyUpdate(metricEvent);
        analysisController.notifyUpdate(metricEvent);
        analysisController.notifyUpdate(metricEvent);

        try {
            assertEquals(ACSAlarm.ERROR, analysisController.verify("foo").unwrap());
        } catch (WrongValueException e) {
            fail(e.getMessage());
        }
    }
}
