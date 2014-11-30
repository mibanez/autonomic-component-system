package cl.niclabs.scada.acs.component.controllers.analysis;

import cl.niclabs.scada.acs.component.ACSManager;
import cl.niclabs.scada.acs.component.controllers.DuplicatedElementIdException;
import cl.niclabs.scada.acs.component.controllers.InvalidElementException;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;
import cl.niclabs.scada.acs.gcmscript.model.CommunicationException;
import org.junit.Assert;
import org.junit.Test;
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
 * Created by mibanez
 */
public class AnalysisControllerImplTest {

    public static class FakeRule implements Serializable {}
    public static class FooRule extends Rule {
        public int counter = -1;
        public ACSAlarm alarm = null;
        public FooRule() throws CommunicationException {
            subscribeTo("foo");
        }
        public ACSAlarm getAlarm(MonitoringController monitoringController) {
            return null;
        }
        public ACSAlarm verify(MonitoringController monitoringController) {
            System.out.println("counter = " + counter);
            counter = (++counter) % 4;
            switch (counter) {
                case 0: alarm = ACSAlarm.OK; break;
                case 1: alarm = ACSAlarm.WARNING; break;
                case 2: alarm = ACSAlarm.VIOLATION; break;
                case 3: alarm = ACSAlarm.ERROR; break;
            }
            System.out.println("counter = " + counter);
            return alarm;
        }
    }

    @Test
    public void rules() throws NoSuchInterfaceException {

        AnalysisControllerImpl analysisController = new AnalysisControllerImpl();
        Component host = mock(PAComponent.class);
        doReturn(analysisController).when(host).getFcInterface(eq(ACSManager.ANALYSIS_CONTROLLER));
        analysisController.setHostComponent(host);

        try {
            MonitoringController monitoringController = Mockito.mock(MonitoringController.class);
            analysisController.bindFc(MonitoringController.ITF_NAME, monitoringController);
        }
        catch (NoSuchInterfaceException e) {
            Assert.fail("Fail when creating the MonitorControllerImpl: " + e.getMessage());
        }

        // wrong class path
        try {
            analysisController.add("foo", "not.a.real.path.rule");
            Assert.fail("WrongValueException expected");
        } catch (InvalidElementException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception");
        }

        // class is not a rule
        try {
            analysisController.add("foo", FakeRule.class.getName());
            Assert.fail("WrongValueException expected");
        } catch (InvalidElementException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception");
        }

        // a correct one
        try {
            analysisController.add("foo", FooRule.class.getName());

            assertEquals(ACSAlarm.OK, analysisController.verify("foo").unwrap());
            assertEquals(ACSAlarm.WARNING, analysisController.verify("foo").unwrap());
            assertEquals(ACSAlarm.VIOLATION, analysisController.verify("foo").unwrap());
            assertEquals(ACSAlarm.ERROR, analysisController.verify("foo").unwrap());

            // add multiple rules
            analysisController.add("foo2", FooRule.class);
            analysisController.add("foo3", FooRule.class);

            HashSet<String> nameSet = new HashSet<>(analysisController.getRegisteredIds());
            Assert.assertTrue(nameSet.contains("foo"));
            Assert.assertTrue(nameSet.contains("foo2"));
            Assert.assertTrue(nameSet.contains("foo3"));

            // removing a rule
            analysisController.remove("foo2");

            nameSet.clear();
            nameSet.addAll(analysisController.getRegisteredIds());
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

        AnalysisControllerImpl analysisController = new AnalysisControllerImpl();

        Component host = mock(PAComponent.class);
        doReturn(analysisController).when(host).getFcInterface(eq(ACSManager.ANALYSIS_CONTROLLER));
        analysisController.setHostComponent(host);

        try {
            MonitoringController monitoringController = Mockito.mock(MonitoringController.class);
            analysisController.bindFc(MonitoringController.ITF_NAME, monitoringController);
        }
        catch (NoSuchInterfaceException e) {
            Assert.fail("Fail when creating the MonitorControllerImpl: " + e.getMessage());
        }

        try {
            analysisController.add("foo", FooRule.class.getName());
        } catch (DuplicatedElementIdException | InvalidElementException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        analysisController.notifyMetricChange("foo");
        analysisController.notifyMetricChange("foo");
        analysisController.notifyMetricChange("foo");

        assertEquals(ACSAlarm.ERROR, analysisController.verify("foo").unwrap());
    }
}
