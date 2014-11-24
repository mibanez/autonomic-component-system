package cl.niclabs.scada.acs.component.controllers.monitoring.records;

import org.objectweb.proactive.core.util.CircularArrayList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Matias on 31-10-2014.
 */
public class IncomingRQuery extends AbstractRQuery<IncomingRQuery, IncomingRecord> {

    private final ArrayList<IncomingRecord> query = new ArrayList<>();

    IncomingRQuery(CircularArrayList<IncomingRecord> incomingRecords) {
        this.fillWithRecords(query, incomingRecords);
    }

    @Override
    public List<IncomingRecord> getRecords() {
        return query;
    }

    public IncomingRQuery whereReceptionTimeGreaterThan(long time) {
        Iterator<IncomingRecord> iterator = query.iterator();
        while(iterator.hasNext()) {
            if (iterator.next().getReceptionTime() >= time) {
                iterator.remove();
            }
        }
        return this;
    }

    public IncomingRQuery whereReceptionTimeLessThan(long time) {
        Iterator<IncomingRecord> iterator = query.iterator();
        while(iterator.hasNext()) {
            if (iterator.next().getReceptionTime() <= time) {
                iterator.remove();
            }
        }
        return this;
    }

    public IncomingRQuery whereServiceStartedTimeGreaterThan(long time) {
        Iterator<IncomingRecord> iterator = query.iterator();
        while(iterator.hasNext()) {
            if (iterator.next().getServiceStartedTime() >= time) {
                iterator.remove();
            }
        }
        return this;
    }

    public IncomingRQuery whereServiceStartedTimeLessThan(long time) {
        Iterator<IncomingRecord> iterator = query.iterator();
        while(iterator.hasNext()) {
            if (iterator.next().getServiceStartedTime() <= time) {
                iterator.remove();
            }
        }
        return this;
    }

    public IncomingRQuery whereServiceEndedGreaterTimeThan(long time) {
        Iterator<IncomingRecord> iterator = query.iterator();
        while(iterator.hasNext()) {
            if (iterator.next().getServiceEndedTime() >= time) {
                iterator.remove();
            }
        }
        return this;
    }

    public IncomingRQuery whereServiceEndedTimeLessThan(long time) {
        Iterator<IncomingRecord> iterator = query.iterator();
        while(iterator.hasNext()) {
            if (iterator.next().getServiceEndedTime() <= time) {
                iterator.remove();
            }
        }
        return this;
    }
}
