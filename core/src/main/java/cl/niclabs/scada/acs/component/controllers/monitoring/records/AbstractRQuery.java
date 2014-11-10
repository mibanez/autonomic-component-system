package cl.niclabs.scada.acs.component.controllers.monitoring.records;

import org.objectweb.proactive.core.util.CircularArrayList;

import java.util.Iterator;
import java.util.List;

abstract class AbstractRQuery<Q extends AbstractRQuery, R extends AbstractRecord> {

    public abstract List<R> getRecords();

    public AbstractRQuery whereRecordIsFinished() {
        Iterator<R> iterator = getRecords().iterator();
        while(iterator.hasNext()) {
            if (!iterator.next().isFinished()) {
                iterator.remove();
            }
        }
        return this;
    }

    public AbstractRQuery whereRecordIsNotFinished() {
        Iterator<R> iterator = getRecords().iterator();
        while(iterator.hasNext()) {
            if (iterator.next().isFinished()) {
                iterator.remove();
            }
        }
        return this;
    }

    public AbstractRQuery whereSourceEquals(String source) {
        Iterator<R> iterator = getRecords().iterator();
        while(iterator.hasNext()) {
            if (!iterator.next().getSource().equals(source)) {
                iterator.remove();
            }
        }
        return this;
    }

    public AbstractRQuery whereDestinationEquals(String destination) {
        Iterator<R> iterator = getRecords().iterator();
        while(iterator.hasNext()) {
            if (!iterator.next().getDestination().equals(destination)) {
                iterator.remove();
            }
        }
        return this;
    }

    public AbstractRQuery whereInterfaceNameEquals(String interfaceName) {
        Iterator<R> iterator = getRecords().iterator();
        while(iterator.hasNext()) {
            if (!iterator.next().getInterfaceName().equals(interfaceName)) {
                iterator.remove();
            }
        }
        return this;
    }

    public AbstractRQuery whereMethodNameEquals(String methodName) {
        Iterator<R> iterator = getRecords().iterator();
        while(iterator.hasNext()) {
            if (!iterator.next().getMethodName().equals(methodName)) {
                iterator.remove();
            }
        }
        return this;
    }

    /** Add all the records to the list */
    protected void fillWithRecords(List<R> query, CircularArrayList records) {
        synchronized (records) {
            for (int i = 0; i < records.size(); i++) {
                query.add((R) records.get(i));
            }
        }
    }
}
