package cl.niclabs.scada.acs.component.controllers.monitoring.metrics;


import cl.niclabs.scada.acs.component.controllers.monitoring.events.RecordEvent;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.gcmscript.model.CommunicationException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

public interface MetricStore {

    static final String ITF_NAME = "metric-store-nf";

    <METRIC extends Metric> Wrapper<Boolean> add(String id, Class<METRIC> clazz);
    Wrapper<Boolean> remove(String id);
    ArrayList<String> getRegisteredIds();

    <VALUE extends Serializable> Wrapper<VALUE> getValue(String metricId);
    <VALUE extends Serializable> Wrapper<VALUE> getValue(String metricId, String... itfNames);
    <VALUE extends Serializable> Wrapper<VALUE> calculate(String metricId);

    Wrapper<Boolean> isEnabled(String metricId);
    Wrapper<Boolean> setEnabled(String metricId, boolean enabled);

    Wrapper<Boolean> subscribeTo(String id, RecordEvent eventType) throws CommunicationException;
    Wrapper<Boolean> unsubscribeFrom(String id, RecordEvent eventType) throws CommunicationException;
    Wrapper<Boolean> isSubscribedTo(String id, RecordEvent eventType) throws CommunicationException;
    Wrapper<HashSet<RecordEvent>> getSubscriptions(String id);

}
