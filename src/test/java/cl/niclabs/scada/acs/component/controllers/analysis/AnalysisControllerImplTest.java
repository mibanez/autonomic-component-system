package cl.niclabs.scada.acs.component.controllers.analysis;

import cl.niclabs.scada.acs.component.controllers.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.monitoring.MetricEvent;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
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

        // wrong class path
        Wrapper<Boolean> wrapper = analysisController.addRule("foo", "not.a.real.path.rule");
        assertEquals(false, wrapper.unwrap());
        assertNotNull(wrapper.getException());

        // class is not a rule
        wrapper = analysisController.addRule("foo", FakeRule.class.getName());
        assertEquals(false, wrapper.unwrap());
        assertNull(wrapper.getException());

        // a correct one
        wrapper = analysisController.addRule("foo", FooRule.class.getName());
        assertTrue(wrapper.unwrap());
        assertEquals(ACSAlarm.OK, analysisController.verify("foo").unwrap());
        assertEquals(ACSAlarm.WARNING, analysisController.verify("foo").unwrap());
        assertEquals(ACSAlarm.VIOLATION, analysisController.verify("foo").unwrap());
        assertEquals(ACSAlarm.ERROR, analysisController.verify("foo").unwrap());

        // add multiple rules
        assertTrue(analysisController.addRule("foo2", FooRule.class).unwrap());
        assertTrue(analysisController.addRule("foo3", FooRule.class).unwrap());

        HashSet<String> nameSet = new HashSet<>();
        Collections.addAll(nameSet, analysisController.getRuleNames().unwrap());
        assertTrue(nameSet.contains("foo"));
        assertTrue(nameSet.contains("foo2"));
        assertTrue(nameSet.contains("foo3"));

        // removing a rule
        assertTrue(analysisController.removeRule("foo2").unwrap());

        nameSet.clear();
        Collections.addAll(nameSet, analysisController.getRuleNames().unwrap());
        assertTrue(nameSet.contains("foo"));
        assertFalse(nameSet.contains("foo2"));
        assertTrue(nameSet.contains("foo3"));
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

        analysisController.addRule("foo", FooRule.class.getName());

        MetricEvent metricEvent = mock(MetricEvent.class);
        analysisController.notifyUpdate(metricEvent);
        analysisController.notifyUpdate(metricEvent);
        analysisController.notifyUpdate(metricEvent);

        assertEquals(ACSAlarm.ERROR, analysisController.verify("foo").unwrap());
    }
}
