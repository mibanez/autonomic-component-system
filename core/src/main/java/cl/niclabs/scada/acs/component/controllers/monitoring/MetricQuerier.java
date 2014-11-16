package cl.niclabs.scada.acs.component.controllers.monitoring;

import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;

import java.io.Serializable;

public class MetricQuerier {

    MonitoringController monitoringController;

    MetricQuerier(MonitoringController monitoringController) {
        this.monitoringController = monitoringController;
    }

    public <VALUE extends Serializable> Wrapper<VALUE> getValue(String metricId) {
        return monitoringController.getValue(metricId);
    }

    public MetricQuerier remoteMonitoring(String interfaceName) {
        MonitoringController remoteMon = monitoringController.remoteMonitoring(interfaceName).unwrap();
        return remoteMon == null ? null : new MetricQuerier(remoteMon);
    }
}
