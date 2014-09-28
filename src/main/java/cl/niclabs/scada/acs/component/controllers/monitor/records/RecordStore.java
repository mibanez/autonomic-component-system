package cl.niclabs.scada.acs.component.controllers.monitor.records;

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
public class RecordStore implements Serializable {

    private final static Logger logger = LoggerFactory.getLogger(RecordStore.class);

    // Stores the records on an optimized way for record attributes queries
    private final CircularArrayList<IncomingRequestRecord> incomingCircleArray = new CircularArrayList<>();
    private final CircularArrayList<OutgoingRequestRecord> outgoingCircleArray = new CircularArrayList<>();
    private final CircularArrayList<OutgoingVoidRequestRecord> outgoingVoidCircleArray = new CircularArrayList<>();

    // Stores the records on an optimized way for record-id queries. Map: record_id --> record
    private final Map<Long, IncomingRequestRecord> incomingMap = new HashMap<>();
    private final Map<Long, OutgoingRequestRecord> outgoingMap = new HashMap<>();
    private final Map<Long, OutgoingVoidRequestRecord> outgoingVoidMap = new HashMap<>();

    // Store default size
    private long maxSize = 64;


    public RecordStore() {

    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public void update(IncomingRequestRecord record) {
        synchronized (incomingCircleArray) {
            IncomingRequestRecord oldRecord = incomingMap.get(record.getCurrentId());
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

    public void update(OutgoingRequestRecord record) {
        synchronized (outgoingCircleArray) {
            OutgoingRequestRecord oldRecord = outgoingMap.get(record.getCurrentId());
            if (oldRecord != null) {
                if (record.getResponseReceivedTime() != 0) {
                    oldRecord.setResponseReceivedTime(record.getResponseReceivedTime());
                }
                if (record.getResponseCompletedTime() != 0) {
                    oldRecord.setResponseCompletedTime(record.getResponseCompletedTime());
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

    public void update(OutgoingVoidRequestRecord record) {
        synchronized (outgoingVoidCircleArray) {
            OutgoingVoidRequestRecord oldRecord = outgoingVoidMap.get(record.getCurrentId());
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

}
