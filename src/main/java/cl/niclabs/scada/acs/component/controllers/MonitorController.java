package cl.niclabs.scada.acs.component.controllers;

import cl.niclabs.scada.acs.component.controllers.monitoring.Metric;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;

public interface MonitorController {

    public Wrapper<Boolean> addMetric(String name, Metric metric);

    public <TYPE> Wrapper<TYPE> getValue(String metricName);

}
