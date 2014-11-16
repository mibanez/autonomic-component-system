package cl.niclabs.scada.acs.component.controllers.monitoring.records;

public interface RecordQuerier {

    RQuery fromAll();
    IncomingRQuery fromIncoming();
    OutgoingRQuery fromOutgoing();
    OutgoingVoidRQuery fromOutgoingVoid();
}
