package cl.niclabs.scada.acs.component.controllers;

import cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm;
import cl.niclabs.scada.acs.component.controllers.planning.Plan;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;

/**
 * Planning controller
 *
 */
public interface PlanningController {

    public Wrapper<Boolean> add(String planId, String className);

    public <PLAN extends Plan> Wrapper<Boolean> add(String planId, Class<PLAN> clazz);

    public Wrapper<Boolean> remove(String planId);

    public void doPlanFor(String planId, ACSAlarm alarm);

    public Wrapper<String[]> getRegisteredIds();

}
