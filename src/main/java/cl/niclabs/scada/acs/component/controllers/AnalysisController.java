package cl.niclabs.scada.acs.component.controllers;

import cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm;
import cl.niclabs.scada.acs.component.controllers.analysis.Rule;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;

/**
 * Analysis Controller
 *
 */
public interface AnalysisController {

    public Wrapper<Boolean> addRule(String name, String className);

    public <RULE extends Rule> Wrapper<Boolean> addRule(String name, Class<RULE> clazz);

    public Wrapper<Boolean> removeRule(String name);

    public Wrapper<ACSAlarm> verify(String name);

}
