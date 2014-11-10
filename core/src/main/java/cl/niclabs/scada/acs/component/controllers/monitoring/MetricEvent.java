package cl.niclabs.scada.acs.component.controllers.monitoring;

import java.io.Serializable;

/**
 * Created by mibanez
 */
public class MetricEvent implements Serializable {

    private final String name;
    private final Class clazz;
    private final Object value;

    <METRIC extends Metric> MetricEvent(String metricName, METRIC metric) {
        name = metricName;
        clazz = metric.getClass();
        value = metric.getValue();
    }

    public String getMetricName() {
        return name;
    }

    public Class getMetricClass() {
        return clazz;
    }

    public Object getMetricValue() {
        return value;
    }

}
