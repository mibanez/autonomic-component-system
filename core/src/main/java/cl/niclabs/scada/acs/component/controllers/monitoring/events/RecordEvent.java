package cl.niclabs.scada.acs.component.controllers.monitoring.events;


import java.io.Serializable;

public enum RecordEvent implements Serializable {

    // Incoming
    REQUEST_RECEIVED,
    REQUEST_SERVICE_STARTED,
    REQUEST_SERVICE_ENDED,

    // Outgoing
    REQUEST_SENT,
    FUTURE_RECEIVED,
    RESPONSE_RECEIVED,

    // Outgoing void
    VOID_REQUEST_SENT,

}
