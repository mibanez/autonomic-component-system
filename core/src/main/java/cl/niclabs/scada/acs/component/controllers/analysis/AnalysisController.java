package cl.niclabs.scada.acs.component.controllers.analysis;

import cl.niclabs.scada.acs.component.controllers.DuplicatedElementIdException;
import cl.niclabs.scada.acs.component.controllers.ElementNotFoundException;
import cl.niclabs.scada.acs.component.controllers.InvalidElementException;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;

import java.util.HashSet;

/**
 * Analysis Controller
 *
 */
public interface AnalysisController {

    void add(String ruleId, String className) throws DuplicatedElementIdException, InvalidElementException;

    <RULE extends Rule> void add(String ruleId, Class<RULE> clazz) throws DuplicatedElementIdException, InvalidElementException;

    void remove(String ruleId) throws ElementNotFoundException;

    HashSet<String> getRegisteredIds();

    // RULE

    Wrapper<ACSAlarm> getAlarm(String id);

    Wrapper<ACSAlarm> verify(String id);

    // STATE

    Wrapper<Boolean> isEnabled(String id);

    Wrapper<Boolean> setEnabled(String id, boolean enabled);

    // SUBSCRIPTION

    Wrapper<Boolean> subscribeTo(String id, String metricName);

    Wrapper<Boolean> unsubscribeFrom(String id, String metricName);

    Wrapper<Boolean> isSubscribedTo(String id, String metricName);

    Wrapper<HashSet<String>> getSubscriptions(String id);

}
