package cl.niclabs.scada.acs.component.controllers.monitor;

/**
 * Created by mibanez
 */
public interface ACSEventListener {

    public void notifyACSEvent(ACSEventType eventType);

}
