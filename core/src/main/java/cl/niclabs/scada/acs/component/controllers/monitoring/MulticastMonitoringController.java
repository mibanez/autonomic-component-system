package cl.niclabs.scada.acs.component.controllers.monitoring;

import cl.niclabs.scada.acs.component.controllers.DuplicatedElementIdException;
import cl.niclabs.scada.acs.component.controllers.InvalidElementException;
import cl.niclabs.scada.acs.component.controllers.monitoring.events.RecordEvent;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.Metric;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.gcmscript.model.CommunicationException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public interface MulticastMonitoringController {

    static final String NAME = "multicast-monitoring-controller-nf";

    // from MonitoringController
    void add(String metricId, String className) throws DuplicatedElementIdException, InvalidElementException;
    void startMonitoring();
    void stopMonitoring();

    // from MetricStore
    <METRIC extends Metric> List<Wrapper<Boolean>> add(String id, Class<METRIC> clazz);
    List<Wrapper<Boolean>> remove(String id);
    List<ArrayList<String>> getRegisteredIds();
    <VALUE extends Serializable> List<Wrapper<VALUE>> getValue(String metricId);
    <VALUE extends Serializable> List<Wrapper<VALUE>> getValue(String metricId, String... itfNames);
    <VALUE extends Serializable> List<Wrapper<VALUE>> calculate(String metricId);
    List<Wrapper<Boolean>> isEnabled(String metricId);
    List<Wrapper<Boolean>> setEnabled(String metricId, Boolean enabled);
    List<Wrapper<Boolean>> subscribeTo(String id, RecordEvent eventType) throws CommunicationException;
    List<Wrapper<Boolean>> unsubscribeFrom(String id, RecordEvent eventType) throws CommunicationException;
    List<Wrapper<Boolean>> isSubscribedTo(String id, RecordEvent eventType) throws CommunicationException;
}
