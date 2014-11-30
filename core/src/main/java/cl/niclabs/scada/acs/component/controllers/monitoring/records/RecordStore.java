package cl.niclabs.scada.acs.component.controllers.monitoring.records;

import java.util.List;

public interface RecordStore {

    static final String ITF_NAME = "record-store-nf";

    void setMaxSize(int maxSize);

    void update(IncomingRecord record);
    void update(OutgoingRecord record);
    void update(OutgoingVoidRecord record);


    List<Record> fromAll(RCondition<Record> condition);

    List<IncomingRecord> getIncoming();
    List<IncomingRecord> getIncoming(RCondition<IncomingRecord> condition);
    List<IncomingRecord> getIncoming(RCondition<IncomingRecord> condition, int limit);
    Long countIncoming();
    Long countIncoming(RCondition<IncomingRecord> condition);

    List<OutgoingRecord> fromOutgoing(RCondition<OutgoingRecord> condition);
    List<OutgoingRecord> fromOutgoing(RCondition<OutgoingRecord> condition, int limit);

    List<OutgoingVoidRecord> fromOutgoingVoid(RCondition<OutgoingVoidRecord> condition);
    List<OutgoingVoidRecord> fromOutgoingVoid(RCondition<OutgoingVoidRecord> condition, int limit);
}
