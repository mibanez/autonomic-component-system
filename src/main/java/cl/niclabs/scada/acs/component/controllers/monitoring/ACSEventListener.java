package cl.niclabs.scada.acs.component.controllers.monitoring;

/**
 * Created by mibanez
 */
public interface ACSEventListener {

    public void notifyACSEvent(ACSEventType eventType);

}
