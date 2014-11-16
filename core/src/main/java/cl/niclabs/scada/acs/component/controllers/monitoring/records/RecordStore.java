package cl.niclabs.scada.acs.component.controllers.monitoring.records;

public interface RecordStore extends RecordQuerier {

    void setMaxSize(int maxSize);

    void update(IncomingRecord record);
    void update(OutgoingRecord record);
    void update(OutgoingVoidRecord record);
}
