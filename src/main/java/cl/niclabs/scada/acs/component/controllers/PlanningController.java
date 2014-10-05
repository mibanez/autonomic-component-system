package cl.niclabs.scada.acs.component.controllers;

/**
 * Planning controller
 *
 */
public interface PlanningController {

    public Plan add(String id, String className) throws DuplicatedElementIdException, InvalidElementException;

    public <PLAN extends Plan> Plan add(String id, Class<PLAN> clazz) throws DuplicatedElementIdException, InvalidElementException;

    public Plan get(String id) throws ElementNotFoundException;

    public void remove(String id) throws ElementNotFoundException;

    public String[] getRegisteredIds();

    public Wrapper<Boolean> doPlanFor(String id, String ruleId, ACSAlarm alarm);

    public Wrapper<Boolean> globalSubscription(String id, ACSAlarm alarmLevel);

    public Wrapper<Boolean> subscribeTo(String id, String ruleId, ACSAlarm alarmLevel);

    public Wrapper<Boolean> unsubscribeFrom(String id, String ruleId);

    public Wrapper<Boolean> isSubscribedTo(String id, String ruleId, ACSAlarm alarmLevel);
}
