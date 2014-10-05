package cl.niclabs.scada.acs.component.controllers.planning;

import cl.niclabs.scada.acs.component.controllers.*;
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
public class PlanningControllerImplTest {

    public static class FakePlan implements Serializable {}
    public static class FooPlan extends Plan {
        public static int counter = 0;
        FooPlan() {
            try {
                subscribeTo("foo-rule", ACSAlarm.VIOLATION);
                globalSubscription(ACSAlarm.ERROR);
            } catch (CommunicationException e) {
                e.printStackTrace();
            }
        }
        public void doPlanFor(String ruleName, ACSAlarm alarm, MonitoringController monitorController) {
            counter++;
        }
    }

    @Test
    public void plans() {

        PlanningControllerImpl planningController = new PlanningControllerImpl();
        try {
            MonitoringController monitoringController = mock(MonitoringController.class);
            planningController.bindFc(PlanningControllerImpl.MONITORING_CONTROLLER_CLIENT_ITF, monitoringController);
        }
        catch (NoSuchInterfaceException e) {
            fail("Fail when creating the MonitorControllerImpl: " + e.getMessage());
        }

        Plan fooPlan = null;
        try {
            // wrong class path
            fooPlan = planningController.add("foo", "not.a.real.path.plan");
            fail("WrongValueException excepted");
        } catch (InvalidElementException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        try {
            // class is not a rule
            fooPlan = planningController.add("foo", FakePlan.class.getName());
            fail("WrongValueException excepted");
        } catch (InvalidElementException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        try {
            // a correct one
            fooPlan = planningController.add("foo", FooPlan.class.getName());

            assertEquals(0, FooPlan.counter);

            // no subscribed events
            fooPlan.doPlanFor("no-subscribed-rule", ACSAlarm.OK, null);
            fooPlan.doPlanFor("no-subscribed-rule", ACSAlarm.WARNING, null);
            fooPlan.doPlanFor("no-subscribed-rule", ACSAlarm.VIOLATION, null);
            fooPlan.doPlanFor("foo-rule", ACSAlarm.OK, null);
            fooPlan.doPlanFor("foo-rule", ACSAlarm.WARNING, null);
            assertEquals(0, FooPlan.counter);

            fooPlan.doPlanFor("foo-rule", ACSAlarm.VIOLATION, null);
            fooPlan.doPlanFor("foo-rule", ACSAlarm.ERROR, null);
            fooPlan.doPlanFor("no-subscribed-rule", ACSAlarm.ERROR, null);
            assertEquals(3, FooPlan.counter);

            // add multiple rules
            planningController.add("foo2", FooPlan.class);
            planningController.add("foo3", FooPlan.class);

            HashSet<String> nameSet = new HashSet<>();
            Collections.addAll(nameSet, planningController.getRegisteredIds());
            assertTrue(nameSet.contains("foo"));
            assertTrue(nameSet.contains("foo2"));
            assertTrue(nameSet.contains("foo3"));

            // removing a rule
            planningController.remove("foo2");

            nameSet.clear();
            Collections.addAll(nameSet, planningController.getRegisteredIds());
            assertTrue(nameSet.contains("foo"));
            assertFalse(nameSet.contains("foo2"));
            assertTrue(nameSet.contains("foo3"));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
