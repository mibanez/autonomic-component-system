package cl.niclabs.scada.acs.component.controllers.monitoring.records;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.component.componentcontroller.AbstractPAComponentController;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

import java.util.*;

/**
 * Stores acs event records
 *
 */
public class RecordStoreImpl extends AbstractPAComponentController implements RecordStore {

    private static final Logger logger = ProActiveLogger.getLogger("ACS");

    private int maxSize = 256;
    private final LinkedList<IncomingRecord> inLinkedList = new LinkedList<>();
    private final LinkedList<OutgoingRecord> outLinkedList = new LinkedList<>();
    private final LinkedList<OutgoingVoidRecord> outVoidLinkedList = new LinkedList<>();

    private final HashMap<Long, IncomingRecord> inMap = new HashMap<>();
    private final HashMap<Long, OutgoingRecord> outMap = new HashMap<>();
    private final HashMap<Long, OutgoingVoidRecord> outVoidMap = new HashMap<>();



    @Override
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public void update(IncomingRecord record) {
        IncomingRecord old = inMap.get(record.getCurrentId());
        if (old == null) {
            inLinkedList.push(record);
            inMap.put(record.getCurrentId(), record);
            if (inLinkedList.size() >= maxSize) {
                inMap.remove(inLinkedList.pollLast().getCurrentId());
            }
        } else {
            if (record.getServiceStartedTime() != 0) {
                old.setServiceStartedTime(record.getServiceStartedTime());
            }
            if (record.getServiceEndedTime() != 0) {
                old.setServiceEndedTime(record.getServiceEndedTime());
            }
            if (record.getReceptionTime() != 0) {
                if (old.getReceptionTime() != 0) {
                    //logger.warn("incoming request record already exist! {}", record);
                } else {
                    old.setReceptionTime(record.getReceptionTime());
                }
            }
        }
    }

    @Override
    public void update(OutgoingRecord record) {
        OutgoingRecord oldRecord = outMap.get(record.getCurrentId());
        if (oldRecord == null) {
            outLinkedList.push(record);
            outMap.put(record.getCurrentId(), record);
            if (outLinkedList.size() >= maxSize) {
                outMap.remove(outLinkedList.pollLast().getCurrentId());
            }
        } else {
            if (record.getFutureReceivedTime() != 0) {
                oldRecord.setFutureReceivedTime(record.getFutureReceivedTime());
            }
            if (record.getResponseReceivedTime() != 0) {
                oldRecord.setResponseReceivedTime(record.getResponseReceivedTime());
            }
            if (record.getSentTime() != 0) {
                if (oldRecord.getSentTime() != 0) {
                    //logger.warn("incoming request record already exist! {}", record);
                } else {
                    oldRecord.setSentTime(record.getSentTime());
                }
            }
        }
    }

    @Override
    public void update(OutgoingVoidRecord record) {
        OutgoingVoidRecord oldRecord = outVoidMap.get(record.getCurrentId());
        if (oldRecord == null) {
            outVoidLinkedList.push(record);
            outVoidMap.put(record.getCurrentId(), record);
            if (outVoidLinkedList.size() >= maxSize) {
                outVoidMap.remove(outVoidLinkedList.pollLast().getCurrentId());
            }
        } else {
            //logger.warn("outgoing void request record already exists! {}", record);
        }
    }

    @Override
    public List<Record> fromAll(RCondition<Record> condition) {
        return null;
    }

    @Override
    public List<IncomingRecord> getIncoming() {
        ArrayList<IncomingRecord> list = new ArrayList<>(inMap.size());
        for (Iterator<IncomingRecord> it = inLinkedList.iterator(); it.hasNext(); ) {
            list.add(it.next());
        }
        return list;
    }

    @Override
    public List<IncomingRecord> getIncoming(RCondition<IncomingRecord> condition) {
        return getIncoming(condition, Integer.MAX_VALUE);
    }

    @Override
    public List<IncomingRecord> getIncoming(RCondition<IncomingRecord> condition, int limit) {
        ArrayList<IncomingRecord> list = new ArrayList<>(inMap.size());
        int c = 0;
        for (IncomingRecord record : inLinkedList) {
            if (condition.evaluate(record)) {
                list.add(record);
                if (++c >= limit) {
                    return list;
                }
            }
        }
        return list;
    }

    @Override
    public Long countIncoming() {
        return (long) inMap.size();
    }

    @Override
    public Long countIncoming(RCondition<IncomingRecord> condition) {
        long c = 0;
        for (IncomingRecord record : inLinkedList) {
            if (condition.evaluate(record)) {
                c++;
            }
        }
        return c;
    }

    @Override
    public List<OutgoingRecord> fromOutgoing(RCondition<OutgoingRecord> condition) {
        return fromOutgoing(condition, Integer.MAX_VALUE);
    }

    @Override
    public List<OutgoingRecord> fromOutgoing(RCondition<OutgoingRecord> condition, int limit) {
        ArrayList<OutgoingRecord> list = new ArrayList<>(outMap.size());
        int c = 0;
        for (OutgoingRecord record : outLinkedList) {
            if (condition.evaluate(record)) {
                list.add(record);
                if (++c >= limit) {
                    return list;
                }
            }
        }
        return list;
    }

    @Override
    public List<OutgoingVoidRecord> fromOutgoingVoid(RCondition<OutgoingVoidRecord> condition) {
        return fromOutgoingVoid(condition, Integer.MAX_VALUE);
    }

    @Override
    public List<OutgoingVoidRecord> fromOutgoingVoid(RCondition<OutgoingVoidRecord> condition, int limit) {
        ArrayList<OutgoingVoidRecord> list = new ArrayList<>(outVoidMap.size());
        int c = 0;
        for (OutgoingVoidRecord record : outVoidLinkedList) {
            if (condition.evaluate(record)) {
                list.add(record);
                if (++c >= limit) {
                    return list;
                }
            }
        }
        return list;
    }
}
