package cl.niclabs.scada.acs.component.controllers.planning;

import cl.niclabs.scada.acs.component.controllers.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm;
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
public class PlanningControllerImplTest {

    public static class FakePlan implements Serializable {}
    public static class FooPlan extends Plan {
        public static int counter = 0;
        FooPlan() {
            subscribeTo("foo-rule", ACSAlarm.VIOLATION);
            globalSubscription(ACSAlarm.ERROR);
        }
        void doPlanFor(String ruleName, ACSAlarm alarm, MonitoringController monitorController) {
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

        // wrong class path
        Wrapper<Boolean> wrapper = planningController.addPlan("foo", "not.a.real.path.plan");
        assertEquals(false, wrapper.unwrap());
        assertNotNull(wrapper.getException());

        // class is not a rule
        wrapper = planningController.addPlan("foo", FakePlan.class.getName());
        assertEquals(false, wrapper.unwrap());
        assertNull(wrapper.getException());

        // a correct one
        wrapper = planningController.addPlan("foo", FooPlan.class.getName());
        assertTrue(wrapper.unwrap());
        assertEquals(0, FooPlan.counter);

        // no subscribed events
        planningController.doPlanFor("no-subscribed-rule", ACSAlarm.OK);
        planningController.doPlanFor("no-subscribed-rule", ACSAlarm.WARNING);
        planningController.doPlanFor("no-subscribed-rule", ACSAlarm.VIOLATION);
        planningController.doPlanFor("foo-rule", ACSAlarm.OK);
        planningController.doPlanFor("foo-rule", ACSAlarm.WARNING);
        assertEquals(0, FooPlan.counter);

        planningController.doPlanFor("foo-rule", ACSAlarm.VIOLATION);
        planningController.doPlanFor("foo-rule", ACSAlarm.ERROR);
        planningController.doPlanFor("no-subscribed-rule", ACSAlarm.ERROR);
        assertEquals(3, FooPlan.counter);

        // add multiple rules
        assertTrue(planningController.addPlan("foo2", FooPlan.class).unwrap());
        assertTrue(planningController.addPlan("foo3", FooPlan.class).unwrap());

        HashSet<String> nameSet = new HashSet<>();
        Collections.addAll(nameSet, planningController.getPlanNames().unwrap());
        assertTrue(nameSet.contains("foo"));
        assertTrue(nameSet.contains("foo2"));
        assertTrue(nameSet.contains("foo3"));

        // removing a rule
        assertTrue(planningController.removePlan("foo2").unwrap());

        nameSet.clear();
        Collections.addAll(nameSet, planningController.getPlanNames().unwrap());
        assertTrue(nameSet.contains("foo"));
        assertFalse(nameSet.contains("foo2"));
        assertTrue(nameSet.contains("foo3"));
    }

}
