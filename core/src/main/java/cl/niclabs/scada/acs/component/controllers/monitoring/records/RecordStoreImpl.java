package cl.niclabs.scada.acs.component.controllers.monitoring.records;

import org.objectweb.proactive.core.util.CircularArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores acs event records
 *
 */
public class RecordStoreImpl implements RecordStore, Serializable {

    private final static Logger logger = LoggerFactory.getLogger(RecordStoreImpl.class);

    // Stores the records on an optimized way for record attributes queries
    private final CircularArrayList<IncomingRecord> incomingCircleArray = new CircularArrayList<>();
    private final CircularArrayList<OutgoingRecord> outgoingCircleArray = new CircularArrayList<>();
    private final CircularArrayList<OutgoingVoidRecord> outgoingVoidCircleArray = new CircularArrayList<>();

    // Stores the records on an optimized way for record-id queries. Map: record_id --> record
    private final Map<Long, IncomingRecord> incomingMap = new HashMap<>();
    private final Map<Long, OutgoingRecord> outgoingMap = new HashMap<>();
    private final Map<Long, OutgoingVoidRecord> outgoingVoidMap = new HashMap<>();

    // Store default size
    private long maxSize = 64;


    public RecordStoreImpl() {

    }

    @Override
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public void update(IncomingRecord record) {
        synchronized (incomingCircleArray) {
            IncomingRecord oldRecord = incomingMap.get(record.getCurrentId());
            if (oldRecord != null) {
                if (record.getServiceStartedTime() != 0) {
                    oldRecord.setServiceStartedTime(record.getServiceStartedTime());
                }
                if (record.getServiceEndedTime() != 0) {
                    oldRecord.setServiceEndedTime(record.getServiceEndedTime());
                }
                if (record.getReceptionTime() != 0) {
                    if (oldRecord.getReceptionTime() != 0) {
                        logger.warn("incoming request record already exist! {}", record);
                    } else {
                        oldRecord.setReceptionTime(record.getReceptionTime());
                    }
                }
            } else {
                if (incomingCircleArray.size() >= maxSize) {
                    oldRecord = incomingCircleArray.remove(0);
                    incomingMap.remove(oldRecord.getCurrentId());
                }
                incomingCircleArray.add(record);
                incomingMap.put(record.getCurrentId(), record);
            }
        }
    }

    @Override
    public void update(OutgoingRecord record) {
        synchronized (outgoingCircleArray) {
            OutgoingRecord oldRecord = outgoingMap.get(record.getCurrentId());
            if (oldRecord != null) {
                if (record.getFutureReceivedTime() != 0) {
                    oldRecord.setFutureReceivedTime(record.getFutureReceivedTime());
                }
                if (record.getResponseReceivedTime() != 0) {
                    oldRecord.setResponseReceivedTime(record.getResponseReceivedTime());
                }
                if (record.getSentTime() != 0) {
                    if (oldRecord.getSentTime() != 0) {
                        logger.warn("incoming request record already exist! {}", record);
                    } else {
                        oldRecord.setSentTime(record.getSentTime());
                    }
                }
            } else {
                if (outgoingCircleArray.size() >= maxSize) {
                    oldRecord = outgoingCircleArray.remove(0);
                    outgoingMap.remove(oldRecord.getCurrentId());
                }
                outgoingCircleArray.add(record);
                outgoingMap.put(record.getCurrentId(), record);
            }
        }
    }

    @Override
    public void update(OutgoingVoidRecord record) {
        synchronized (outgoingVoidCircleArray) {
            OutgoingVoidRecord oldRecord = outgoingVoidMap.get(record.getCurrentId());
            if (oldRecord != null) {
                logger.warn("outgoing void request record already exists! {}", record);
            } else {
                if (outgoingVoidCircleArray.size() >= maxSize) {
                    oldRecord = outgoingVoidCircleArray.remove(0);
                    outgoingVoidMap.remove(oldRecord.getCurrentId());
                }
                outgoingVoidCircleArray.add(record);
                outgoingVoidMap.put(record.getCurrentId(), record);
            }
        }
    }

    @Override
    public RQuery fromAll() {
        return new RQuery(incomingCircleArray, outgoingCircleArray, outgoingVoidCircleArray);
    }

    @Override
    public IncomingRQuery fromIncoming() {
        return new IncomingRQuery(incomingCircleArray);
    }

    @Override
    public OutgoingRQuery fromOutgoing() {
        return new OutgoingRQuery(outgoingCircleArray);
    }

    @Override
    public OutgoingVoidRQuery fromOutgoingVoid() {
        return new OutgoingVoidRQuery(outgoingVoidCircleArray);
    }
}
