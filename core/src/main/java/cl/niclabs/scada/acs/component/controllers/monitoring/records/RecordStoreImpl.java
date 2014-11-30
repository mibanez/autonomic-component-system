package cl.niclabs.scada.acs.component.controllers.monitoring.records;

import org.objectweb.proactive.core.component.componentcontroller.AbstractPAComponentController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Stores acs event records
 *
 */
public class RecordStoreImpl extends AbstractPAComponentController implements RecordStore {

    private final static Logger logger = LoggerFactory.getLogger(RecordStoreImpl.class);

    static class RNode<TYPE extends Record> implements Serializable {
        TYPE record;
        RNode<TYPE> prev, next;
        RNode(TYPE r, RNode<TYPE> p, RNode<TYPE> n) {record = r; prev = p; next = n;}
    }

    private final RNode<IncomingRecord> iHead = new RNode<>(null, null, null);
    private final RNode<OutgoingRecord> oHead = new RNode<>(null, null, null);
    private final RNode<OutgoingVoidRecord> ovHead = new RNode<>(null, null, null);

    private final HashMap<Long, IncomingRecord> iMap = new HashMap<>();
    private final HashMap<Long, OutgoingRecord> oMap = new HashMap<>();
    private final HashMap<Long, OutgoingVoidRecord> ovMap = new HashMap<>();

    private int maxSize = 256;

    @Override
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public void update(IncomingRecord record) {
        IncomingRecord old = iMap.get(record.getCurrentId());
        if (old != null) {
            if (record.getServiceStartedTime() != 0) {
                old.setServiceStartedTime(record.getServiceStartedTime());
            }
            if (record.getServiceEndedTime() != 0) {
                old.setServiceEndedTime(record.getServiceEndedTime());
            }
            if (record.getReceptionTime() != 0) {
                if (old.getReceptionTime() != 0) {
                    logger.warn("incoming request record already exist! {}", record);
                } else {
                    old.setReceptionTime(record.getReceptionTime());
                }
            }
        } else {
            if (iMap.size() >= maxSize) {
                iMap.remove(iHead.prev.record.getCurrentId());
                iHead.prev.prev.next = iHead; // assume maxSize >= 2
                iHead.prev = iHead.prev.prev;
            }
            if (iMap.size() > 0) {
                RNode<IncomingRecord> n = new RNode<>(record, iHead, iHead.next);
                iHead.next.prev = n;
                iHead.next = n;
                iMap.put(n.record.getCurrentId(), n.record);
            } else {
                iHead.next = new RNode<>(record, iHead, iHead);
                iHead.prev = iHead.next;
                iMap.put(iHead.next.record.getCurrentId(), iHead.next.record);
            }
        }
    }

    @Override
    public void update(OutgoingRecord record) {
        OutgoingRecord oldRecord = oMap.get(record.getCurrentId());
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
            if (oMap.size() >= maxSize) {
                oMap.remove(oHead.prev.record.getCurrentId());
                oHead.prev.prev.next = oHead; // assume maxSize >= 2
                oHead.prev = oHead.prev.prev;
            }
            if (oMap.size() > 0) {
                RNode<OutgoingRecord> n = new RNode<>(record, oHead, oHead.next);
                oHead.next.prev = n;
                oHead.next = n;
                oMap.put(n.record.getCurrentId(), n.record);
            } else {
                oHead.next = new RNode<>(record, oHead, oHead);
                oHead.prev = oHead.next;
                oMap.put(oHead.next.record.getCurrentId(), oHead.next.record);
            }
        }
    }

    @Override
    public void update(OutgoingVoidRecord record) {
        OutgoingVoidRecord oldRecord = ovMap.get(record.getCurrentId());
        if (oldRecord != null) {
            logger.warn("outgoing void request record already exists! {}", record);
        } else {
            if (ovMap.size() >= maxSize) {
                ovMap.remove(ovHead.prev.record.getCurrentId());
                ovHead.prev.prev.next = ovHead; // assume maxSize >= 2
                ovHead.prev = ovHead.prev.prev;
            }
            if (oMap.size() > 0) {
                RNode<OutgoingVoidRecord> n = new RNode<>(record, ovHead, ovHead.next);
                ovHead.next.prev = n;
                ovHead.next = n;
                ovMap.put(n.record.getCurrentId(), n.record);
            } else {
                ovHead.next = new RNode<>(record, ovHead, ovHead);
                ovHead.prev = ovHead.next;
                ovMap.put(ovHead.next.record.getCurrentId(), ovHead.next.record);
            }
        }
    }

    @Override
    public List<Record> fromAll(RCondition<Record> condition) {
        return null;
    }

    @Override
    public List<IncomingRecord> getIncoming() {
        ArrayList<IncomingRecord> list = new ArrayList<>(iMap.size());
        for (RNode<IncomingRecord> n = iHead.next; n != iHead; n = n.next) {
            list.add(n.record);
        }
        return list;
    }

    @Override
    public List<IncomingRecord> getIncoming(RCondition<IncomingRecord> condition) {
        return getIncoming(condition, Integer.MAX_VALUE);
    }

    @Override
    public List<IncomingRecord> getIncoming(RCondition<IncomingRecord> condition, int limit) {
        ArrayList<IncomingRecord> list = new ArrayList<>(iMap.size());
        int c = 0;
        for (RNode<IncomingRecord> n = iHead.next; n != iHead; n = n.next) {
            if (condition.evaluate(n.record)) {
                list.add(n.record);
                if (++c >= limit) {
                    return list;
                }
            }
        }
        return list;
    }

    @Override
    public Long countIncoming() {
        return (long) iMap.size();
    }

    @Override
    public Long countIncoming(RCondition<IncomingRecord> condition) {
        long c = 0;
        for (RNode<IncomingRecord> n = iHead.next; n != iHead; n = n.next) {
            if (condition.evaluate(n.record)) {
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
        ArrayList<OutgoingRecord> list = new ArrayList<>(oMap.size());
        int c = 0;
        for (RNode<OutgoingRecord> n = oHead.next; n != oHead; n = n.next) {
            if (condition.evaluate(n.record)) {
                list.add(n.record);
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
        ArrayList<OutgoingVoidRecord> list = new ArrayList<>(ovMap.size());
        int c = 0;
        for (RNode<OutgoingVoidRecord> n = ovHead.next; n != ovHead; n = n.next) {
            if (condition.evaluate(n.record)) {
                list.add(n.record);
                if (++c >= limit) {
                    return list;
                }
            }
        }
        return list;
    }
}
