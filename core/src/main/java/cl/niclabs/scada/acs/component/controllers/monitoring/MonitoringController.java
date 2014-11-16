package cl.niclabs.scada.acs.component.controllers.monitoring;

import cl.niclabs.scada.acs.component.controllers.DuplicatedElementIdException;
import cl.niclabs.scada.acs.component.controllers.ElementNotFoundException;
import cl.niclabs.scada.acs.component.controllers.InvalidElementException;
import cl.niclabs.scada.acs.component.controllers.monitoring.events.RecordEvent;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.gcmscript.CommunicationException;

import java.io.Serializable;
import java.util.ArrayList;

public interface MonitoringController extends Serializable {

    // METRICS
    void add(String metricId, String className) throws DuplicatedElementIdException, InvalidElementException;
    <METRIC extends Metric> void add(String metricId, Class<METRIC> clazz) throws DuplicatedElementIdException, InvalidElementException;
    void remove(String metricId) throws ElementNotFoundException;
    ArrayList<String> getRegisteredIds();

    Wrapper<Boolean> isEnabled(String metricId);
    Wrapper<Boolean> setEnabled(String metricId, boolean enabled);

    Wrapper<Boolean> subscribeTo(String id, RecordEvent eventType) throws CommunicationException;
    Wrapper<Boolean> unsubscribeFrom(String id, RecordEvent eventType) throws CommunicationException;
    Wrapper<Boolean> isSubscribedTo(String id, RecordEvent eventType) throws CommunicationException;

    <VALUE extends Serializable> Wrapper<VALUE> getValue(String metricId);
    <VALUE extends Serializable> Wrapper<VALUE> calculate(String metricId);

    // REMOTE MONITORING
    Wrapper<MonitoringController> remoteMonitoring(String interfaceName);
}
