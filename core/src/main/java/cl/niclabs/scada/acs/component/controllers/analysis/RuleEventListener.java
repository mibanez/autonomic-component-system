package cl.niclabs.scada.acs.component.controllers.analysis;

/**
 * Created by mibanez
 */
public interface RuleEventListener {

    public void notifyAlarm(RuleEvent event);

}
