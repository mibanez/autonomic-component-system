package cl.niclabs.scada.acs.component.controllers.monitoring.records;

import org.objectweb.proactive.core.util.CircularArrayList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Matias on 09-11-2014.
 */
public class OutgoingRQuery extends AbstractRQuery<OutgoingRQuery, OutgoingRecord> {

    private final ArrayList<OutgoingRecord> query = new ArrayList<>();

    OutgoingRQuery(CircularArrayList<OutgoingRecord> outgoingRecords) {
        this.fillWithRecords(query, outgoingRecords);
    }

    @Override
    public List<OutgoingRecord> getRecords() {
        return query;
    }

    public OutgoingRQuery whereSentTimeGreaterThan(long time) {
        Iterator<OutgoingRecord> iterator = query.iterator();
        while(iterator.hasNext()) {
            if (iterator.next().getSentTime() >= time) {
                iterator.remove();
            }
        }
        return this;
    }

    public OutgoingRQuery whereSentTimeLessThan(long time) {
        Iterator<OutgoingRecord> iterator = query.iterator();
        while(iterator.hasNext()) {
            if (iterator.next().getSentTime() <= time) {
                iterator.remove();
            }
        }
        return this;
    }

    public OutgoingRQuery whereFutureReceivedTimeGreaterThan(long time) {
        Iterator<OutgoingRecord> iterator = query.iterator();
        while(iterator.hasNext()) {
            if (iterator.next().getFutureReceivedTime() >= time) {
                iterator.remove();
            }
        }
        return this;
    }

    public OutgoingRQuery whereFutureReceivedTimeLessThan(long time) {
        Iterator<OutgoingRecord> iterator = query.iterator();
        while(iterator.hasNext()) {
            if (iterator.next().getFutureReceivedTime() <= time) {
                iterator.remove();
            }
        }
        return this;
    }

    public OutgoingRQuery whereResponseReceivedTimeGreaterThan(long time) {
        Iterator<OutgoingRecord> iterator = query.iterator();
        while(iterator.hasNext()) {
            if (iterator.next().getResponseReceivedTime() >= time) {
                iterator.remove();
            }
        }
        return this;
    }

    public OutgoingRQuery whereResponseReceivedTimeLessThan(long time) {
        Iterator<OutgoingRecord> iterator = query.iterator();
        while(iterator.hasNext()) {
            if (iterator.next().getResponseReceivedTime() <= time) {
                iterator.remove();
            }
        }
        return this;
    }
}
