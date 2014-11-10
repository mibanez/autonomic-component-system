package cl.niclabs.scada.acs.component.controllers.planning;

import cl.niclabs.scada.acs.component.ACSUtils;
import cl.niclabs.scada.acs.component.controllers.*;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.component.identity.PAComponent;

import java.io.Serializable;
import java.util.HashSet;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Created by mibanez
 */
public class PlanningControllerImplTest {

    public static class FakePlan implements Serializable {}
    public static class FooPlan extends Plan {
        public static int counter = 0;
        FooPlan() {
            subscribeTo("foo-rule", ACSAlarm.VIOLATION);
            globallySubscribe(ACSAlarm.ERROR);
        }
        public void doPlanFor(String ruleName, ACSAlarm alarm, MonitoringController monitorController) {
            counter++;
        }
    }

    @Test
    public void plans() throws NoSuchInterfaceException {

        PlanningControllerImpl planningController = new PlanningControllerImpl();
        Component host = mock(PAComponent.class);
        doReturn(planningController).when(host).getFcInterface(eq(ACSUtils.PLANNING_CONTROLLER));
        planningController.setHostComponent(host);

        try {
            MonitoringController monitoringController = Mockito.mock(MonitoringController.class);
            planningController.bindFc(PlanningControllerImpl.MONITORING_CONTROLLER_CLIENT_ITF, monitoringController);
        }
        catch (NoSuchInterfaceException e) {
            Assert.fail("Fail when creating the MonitorControllerImpl: " + e.getMessage());
        }

        PlanProxy fooPlan = null;
        try {
            // wrong class path
            fooPlan = planningController.add("foo", "not.a.real.path.plan");
            Assert.fail("WrongValueException excepted");
        } catch (InvalidElementException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        try {
            // class is not a rule
            fooPlan = planningController.add("foo", FakePlan.class.getName());
            Assert.fail("WrongValueException excepted");
        } catch (InvalidElementException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        try {
            // a correct one
            fooPlan = planningController.add("foo", FooPlan.class.getName());

            Assert.assertEquals(0, FooPlan.counter);

            // no subscribed events
            fooPlan.doPlanFor("no-subscribed-rule", ACSAlarm.OK);
            fooPlan.doPlanFor("no-subscribed-rule", ACSAlarm.WARNING);
            fooPlan.doPlanFor("no-subscribed-rule", ACSAlarm.VIOLATION);
            fooPlan.doPlanFor("foo-rule", ACSAlarm.OK);
            fooPlan.doPlanFor("foo-rule", ACSAlarm.WARNING);
            Assert.assertEquals(0, FooPlan.counter);

            fooPlan.doPlanFor("foo-rule", ACSAlarm.VIOLATION);
            fooPlan.doPlanFor("foo-rule", ACSAlarm.ERROR);
            fooPlan.doPlanFor("no-subscribed-rule", ACSAlarm.ERROR);
            Assert.assertEquals(3, FooPlan.counter);

            // add multiple rules
            planningController.add("foo2", FooPlan.class);
            planningController.add("foo3", FooPlan.class);

            HashSet<String> nameSet = new HashSet<>(planningController.getRegisteredIds());
            Assert.assertTrue(nameSet.contains("foo"));
            Assert.assertTrue(nameSet.contains("foo2"));
            Assert.assertTrue(nameSet.contains("foo3"));

            // removing a rule
            planningController.remove("foo2");

            nameSet.clear();
            nameSet.addAll(planningController.getRegisteredIds());
            Assert.assertTrue(nameSet.contains("foo"));
            Assert.assertFalse(nameSet.contains("foo2"));
            Assert.assertTrue(nameSet.contains("foo3"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

}
