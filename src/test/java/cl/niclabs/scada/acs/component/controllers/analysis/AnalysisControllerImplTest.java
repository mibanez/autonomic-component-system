package cl.niclabs.scada.acs.component.controllers.analysis;

import cl.niclabs.scada.acs.component.controllers.*;
import cl.niclabs.scada.acs.component.controllers.monitoring.MetricEvent;
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
        public FooRule() throws CommunicationException {
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

        Rule fooRule = null;

        // wrong class path
        try {
            fooRule = analysisController.add("foo", "not.a.real.path.rule");
            fail("WrongValueException expected");
        } catch (InvalidElementException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception");
        }

        // class is not a rule
        try {
            fooRule = analysisController.add("foo", FakeRule.class.getName());
            fail("WrongValueException expected");
        } catch (InvalidElementException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception");
        }

        // a correct one
        try {
            fooRule = analysisController.add("foo", FooRule.class.getName());

            assertEquals(ACSAlarm.OK, fooRule.verify(null));
            assertEquals(ACSAlarm.WARNING, fooRule.verify(null));
            assertEquals(ACSAlarm.VIOLATION, fooRule.verify(null));
            assertEquals(ACSAlarm.ERROR, fooRule.verify(null));

            // add multiple rules
            analysisController.add("foo2", FooRule.class);
            analysisController.add("foo3", FooRule.class);

            HashSet<String> nameSet = new HashSet<>();
            Collections.addAll(nameSet, analysisController.getRegisteredIds());
            assertTrue(nameSet.contains("foo"));
            assertTrue(nameSet.contains("foo2"));
            assertTrue(nameSet.contains("foo3"));

            // removing a rule
            analysisController.remove("foo2");

            nameSet.clear();
            Collections.addAll(nameSet, analysisController.getRegisteredIds());
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

        AnalysisControllerImpl analysisController = new AnalysisControllerImpl();
        try {
            MonitoringController monitoringController = mock(MonitoringController.class);
            analysisController.bindFc(AnalysisControllerImpl.MONITORING_CONTROLLER_CLIENT_ITF, monitoringController);
        }
        catch (NoSuchInterfaceException e) {
            fail("Fail when creating the MonitorControllerImpl: " + e.getMessage());
        }

        try {
            analysisController.add("foo", FooRule.class.getName());
        } catch (DuplicatedElementIdException | InvalidElementException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        MetricEvent metricEvent = mock(MetricEvent.class);
        analysisController.notifyUpdate(metricEvent);
        analysisController.notifyUpdate(metricEvent);
        analysisController.notifyUpdate(metricEvent);

        try {
            assertEquals(ACSAlarm.ERROR, analysisController.verify("foo").unwrap());
        } catch (CommunicationException e) {
            fail(e.getMessage());
        }
    }
}
