package cl.niclabs.scada.acs.component.controllers;

import cl.niclabs.scada.acs.component.controllers.monitoring.Metric;
import cl.niclabs.scada.acs.component.controllers.monitoring.RecordEvent;

import java.io.Serializable;
import java.util.HashSet;

public interface MonitoringController {

    public MetricProxy add(String metricId, String className) throws DuplicatedElementIdException, InvalidElementException;

    public <METRIC extends Metric> MetricProxy add(String metricId, Class<METRIC> clazz) throws DuplicatedElementIdException, InvalidElementException;

    public void remove(String metricId) throws ElementNotFoundException;

    public HashSet<String> getRegisteredIds();

    // METRIC

    public <VALUE extends Serializable> Wrapper<VALUE> calculate(String metricId);

    public <VALUE extends Serializable> Wrapper<VALUE> getValue(String metricId);

    // STATE

    public Wrapper<Boolean> isEnabled(String metricId);

    public Wrapper<Boolean> setEnabled(String metricId, boolean enabled);

    // SUBSCRIPTION

    public Wrapper<Boolean> subscribeTo(String id, RecordEvent eventType) throws CommunicationException;

    public Wrapper<Boolean> unsubscribeFrom(String id, RecordEvent eventType) throws CommunicationException;

    public Wrapper<Boolean> isSubscribedTo(String id, RecordEvent eventType) throws CommunicationException;

}
