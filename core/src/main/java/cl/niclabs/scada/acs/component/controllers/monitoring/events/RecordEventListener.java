package cl.niclabs.scada.acs.component.controllers.monitoring.events;

/**
 * Created by mibanez
 */
public interface RecordEventListener {

    static final String ITF_NAME = "record-event-listener-nf";

    void notifyRecordEvent(RecordEvent eventType);

}
