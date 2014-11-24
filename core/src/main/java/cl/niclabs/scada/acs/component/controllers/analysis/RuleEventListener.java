package cl.niclabs.scada.acs.component.controllers.analysis;

/**
 * Created by mibanez
 */
public interface RuleEventListener {

    static final String ITF_NAME = "rule-event-listener";

    void notifyAlarm(RuleEvent event);
}
