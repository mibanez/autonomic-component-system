package cl.niclabs.scada.acs.component.controllers.monitoring;

import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;

import java.io.Serializable;
import java.util.HashSet;


public interface MetricStore {

    public HashSet<String> getRegisteredIds();

    public <VALUE extends Serializable> Wrapper<VALUE> getValue(String metricId);

    public <VALUE extends Serializable> Wrapper<VALUE> calculate(String metricId);
}
