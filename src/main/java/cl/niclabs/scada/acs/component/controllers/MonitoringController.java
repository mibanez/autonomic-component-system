package cl.niclabs.scada.acs.component.controllers;

import cl.niclabs.scada.acs.component.controllers.monitoring.Metric;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;

import java.io.Serializable;

public interface MonitoringController {

    public Wrapper<Boolean> add(String metricId, String className);

    public <METRIC extends Metric> Wrapper<Boolean> add(String metricId, Class<METRIC> clazz);

    public <VALUE extends Serializable> Wrapper<VALUE> measure(String metricId);

    public <VALUE extends Serializable> Wrapper<VALUE> getValue(String metricId);

    public Wrapper<Boolean> isEnabled(String metricId);

    public Wrapper<Boolean> enable(String metricId);

    public Wrapper<Boolean> disable(String metricId);

    public Wrapper<Boolean> remove(String metricId);

    public Wrapper<String[]> getRegisteredIds();

}
