package cl.niclabs.scada.acs.component.controllers.monitoring;

import cl.niclabs.scada.acs.component.controllers.DuplicatedElementIdException;
import cl.niclabs.scada.acs.component.controllers.InvalidElementException;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.MetricStore;

import java.io.Serializable;

public interface MonitoringController extends MetricStore, Serializable {

    static final String ITF_NAME = "monitoring-controller-nf";

    void add(String metricId, String className) throws DuplicatedElementIdException, InvalidElementException;

    void startMonitoring();
    void stopMonitoring();
}
