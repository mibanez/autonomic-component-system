package cl.niclabs.scada.acs.component.controllers;

import cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm;
import cl.niclabs.scada.acs.component.controllers.analysis.Rule;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;

/**
 * Analysis Controller
 *
 */
public interface AnalysisController {

    public Wrapper<Boolean> add(String ruleId, String className);

    public <RULE extends Rule> Wrapper<Boolean> add(String ruleId, Class<RULE> clazz);

    public Wrapper<Boolean> remove(String ruleId);

    public Wrapper<ACSAlarm> verify(String ruleId);

    public Wrapper<String[]> getRegisteredIds();

}
