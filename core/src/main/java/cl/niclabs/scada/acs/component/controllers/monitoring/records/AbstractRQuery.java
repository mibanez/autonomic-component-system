package cl.niclabs.scada.acs.component.controllers.monitoring.records;

import org.objectweb.proactive.core.util.CircularArrayList;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

abstract class AbstractRQuery<Q extends AbstractRQuery, R extends AbstractRecord> implements Serializable {

    protected abstract List<R> getRecords();

    public Q whereRecordIsFinished() {
        Iterator<R> iterator = getRecords().iterator();
        while(iterator.hasNext()) {
            if (!iterator.next().isFinished()) {
                iterator.remove();
            }
        }
        return (Q) this;
    }

    public Q whereRecordIsNotFinished() {
        Iterator<R> iterator = getRecords().iterator();
        while(iterator.hasNext()) {
            if (iterator.next().isFinished()) {
                iterator.remove();
            }
        }
        return (Q) this;
    }

    public Q whereSourceEquals(String source) {
        Iterator<R> iterator = getRecords().iterator();
        while(iterator.hasNext()) {
            if (!iterator.next().getSource().equals(source)) {
                iterator.remove();
            }
        }
        return (Q) this;
    }

    public Q whereDestinationEquals(String destination) {
        Iterator<R> iterator = getRecords().iterator();
        while(iterator.hasNext()) {
            if (!iterator.next().getDestination().equals(destination)) {
                iterator.remove();
            }
        }
        return (Q) this;
    }

    public Q whereInterfaceNameEquals(String interfaceName) {
        Iterator<R> iterator = getRecords().iterator();
        while(iterator.hasNext()) {
            if (!iterator.next().getInterfaceName().equals(interfaceName)) {
                iterator.remove();
            }
        }
        return (Q) this;
    }

    public Q whereMethodNameEquals(String methodName) {
        Iterator<R> iterator = getRecords().iterator();
        while(iterator.hasNext()) {
            if (!iterator.next().getMethodName().equals(methodName)) {
                iterator.remove();
            }
        }
        return (Q) this;
    }

    /** Add all the records to the list */
    void fillWithRecords(List<R> query, CircularArrayList records) {
        synchronized (records) {
            for (Object record : records) {
                query.add((R) record);
            }
        }
    }
}
