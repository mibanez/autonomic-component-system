package cl.niclabs.scada.acs.component.controllers.monitoring.records;

public interface RecordStore {

    static final String ITF_NAME = "record-store-nf";

    void setMaxSize(int maxSize);

    void update(IncomingRecord record);
    void update(OutgoingRecord record);
    void update(OutgoingVoidRecord record);

    RQuery fromAll();
    IncomingRQuery fromIncoming();
    OutgoingRQuery fromOutgoing();
    OutgoingVoidRQuery fromOutgoingVoid();
}
