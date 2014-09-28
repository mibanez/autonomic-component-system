package cl.niclabs.scada.acs.component.controllers.monitoring;


public enum ACSEventType {

    // Incoming
    REQUEST_RECEIVED,
    REQUEST_SERVICE_STARTED,
    REQUEST_SERVICE_ENDED,

    // Outgoing
    REQUEST_SENT,
    REQUEST_RESPONSE_RECEIVED,
    REQUEST_RESPONSE_COMPLETED,

    // Outgoing void
    VOID_REQUEST_SENT,

}
