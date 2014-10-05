package cl.niclabs.scada.acs.component.controllers;

import cl.niclabs.scada.acs.component.controllers.monitoring.ACSEventType;

import java.io.Serializable;

public interface MonitoringController {

    public Metric add(String metricId, String className) throws DuplicatedElementIdException, InvalidElementException;

    public <METRIC extends Metric> Metric add(String metricId, Class<METRIC> clazz) throws DuplicatedElementIdException, InvalidElementException;

    public Metric get(String metricId) throws ElementNotFoundException;

    public void remove(String metricId) throws ElementNotFoundException;

    public String[] getRegisteredIds();


    public <VALUE extends Serializable> Wrapper<VALUE> measure(String metricId);

    public <VALUE extends Serializable> Wrapper<VALUE> getValue(String metricId);

    public Wrapper<Boolean> isEnabled(String metricId);

    public Wrapper<Boolean> setEnabled(String metricId, boolean enabled);

    public Wrapper<Boolean> subscribeTo(String id, ACSEventType eventType) throws CommunicationException;

    public Wrapper<Boolean> unsubscribeFrom(String id, ACSEventType eventType) throws CommunicationException;

    public Wrapper<Boolean> isSubscribedTo(String id, ACSEventType eventType) throws CommunicationException;

}
