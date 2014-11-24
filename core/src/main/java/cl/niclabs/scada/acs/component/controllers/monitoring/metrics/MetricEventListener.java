package cl.niclabs.scada.acs.component.controllers.monitoring.metrics;

/**
 * Created by mibanez
 */
public interface MetricEventListener {

    static final String ITF_NAME = "metric-event-listener-nf";

    void notifyMetricChange(String metricName);

}
