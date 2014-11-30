package cl.niclabs.scada.acs.component.controllers.planning;

import cl.niclabs.scada.acs.component.controllers.DuplicatedElementIdException;
import cl.niclabs.scada.acs.component.controllers.ElementNotFoundException;
import cl.niclabs.scada.acs.component.controllers.InvalidElementException;
import cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;

import java.util.HashSet;

/**
 * Planning controller
 *
 */
public interface PlanningController {

    static final String ITF_NAME = "planning-controller-nf";

    public void add(String id, String className) throws DuplicatedElementIdException, InvalidElementException;

    public <PLAN extends Plan> void add(String id, Class<PLAN> clazz) throws DuplicatedElementIdException, InvalidElementException;

    public void remove(String id) throws ElementNotFoundException;

    public HashSet<String> getRegisteredIds();

    // PLANNING

    Wrapper<Boolean> doPlanFor(String id, String ruleId, ACSAlarm alarm);

    // SUBSCRIPTIONS

    public Wrapper<Boolean> subscribeTo(String id, String ruleId);

    public Wrapper<Boolean> unsubscribeFrom(String id, String ruleId);

    public Wrapper<HashSet<String>> getSubscriptions(String id);

    public Wrapper<Boolean> isSubscribedTo(String id, String ruleId);

    // STATE

    public Wrapper<Boolean> isEnabled(String id);

    public Wrapper<Boolean> setEnabled(String id, boolean enabled);
}
