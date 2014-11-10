package cl.niclabs.scada.acs.component.controllers.monitoring.records;

import org.objectweb.proactive.core.util.CircularArrayList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Matias on 09-11-2014.
 */
public class OutgoingVoidRQuery extends AbstractRQuery<OutgoingVoidRQuery, OutgoingVoidRecord> {

    private final ArrayList<OutgoingVoidRecord> query = new ArrayList<>();

    OutgoingVoidRQuery(CircularArrayList<OutgoingVoidRecord> records) {
        fillWithRecords(query, records);
    }

    @Override
    public List<OutgoingVoidRecord> getRecords() {
        return query;
    }

    public AbstractRQuery whereSentTimeGreaterThan(long time) {
        Iterator<OutgoingVoidRecord> iterator = query.iterator();
        while(iterator.hasNext()) {
            if (iterator.next().getSentTime() >= time) {
                iterator.remove();
            }
        }
        return this;
    }

    public AbstractRQuery whereSentTimeLessThan(long time) {
        Iterator<OutgoingVoidRecord> iterator = query.iterator();
        while(iterator.hasNext()) {
            if (iterator.next().getSentTime() <= time) {
                iterator.remove();
            }
        }
        return this;
    }

}
