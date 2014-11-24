package cl.niclabs.scada.acs.component.controllers.monitoring.records;

import org.objectweb.proactive.core.util.CircularArrayList;

import java.util.ArrayList;
import java.util.List;

/**
 * RQuery
 * Created by Matias on 09-11-2014.
 */
public class RQuery extends AbstractRQuery<RQuery, AbstractRecord> {

    private final ArrayList<AbstractRecord> query = new ArrayList<>();

    <R extends AbstractRecord>
    RQuery(CircularArrayList<IncomingRecord> incoming, CircularArrayList<OutgoingRecord> outgoing,
           CircularArrayList<OutgoingVoidRecord> outgoingVoid) {
        fillWithRecords(query, incoming);
        fillWithRecords(query, outgoing);
        fillWithRecords(query, outgoingVoid);
    }

    @Override
    public List<AbstractRecord> getRecords() {
        return query;
    }
}
