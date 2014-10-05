package cl.niclabs.scada.acs.component.controllers;

/**
 * Analysis Controller
 *
 */
public interface AnalysisController {

    public Rule add(String ruleId, String className) throws DuplicatedElementIdException, InvalidElementException;

    public <RULE extends Rule> Rule add(String ruleId, Class<RULE> clazz) throws DuplicatedElementIdException, InvalidElementException;

    public Rule get(String ruleId) throws ElementNotFoundException;

    public void remove(String ruleId) throws ElementNotFoundException;

    public String[] getRegisteredIds();


    public Wrapper<ACSAlarm> verify(String id);

    public Wrapper<Boolean> subscribeTo(String id, String metricName);

    public Wrapper<Boolean> unsubscribeFrom(String id, String metricName);

    public Wrapper<Boolean> isSubscribedTo(String id, String metricName);

}
