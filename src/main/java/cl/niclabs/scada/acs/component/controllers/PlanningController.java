package cl.niclabs.scada.acs.component.controllers;

import cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm;
import cl.niclabs.scada.acs.component.controllers.planning.Plan;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;

/**
 * Planning controller
 *
 */
public interface PlanningController {

    public Wrapper<Boolean> addPlan(String name, String className);

    public <PLAN extends Plan> Wrapper<Boolean> addPlan(String name, Class<PLAN> clazz);

    public Wrapper<Boolean> removePlan(String planName);

    public void doPlanFor(String ruleName, ACSAlarm alarm);

    public Wrapper<String[]> getPlanNames();

}
