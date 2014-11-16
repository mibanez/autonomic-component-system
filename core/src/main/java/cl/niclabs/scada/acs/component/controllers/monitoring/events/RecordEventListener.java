package cl.niclabs.scada.acs.component.controllers.monitoring.events;

/**
 * Created by mibanez
 */
public interface RecordEventListener {

    public void notifyACSEvent(RecordEvent eventType);

}
