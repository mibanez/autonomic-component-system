package cl.niclabs.scada.acs.component.controllers.monitor;

import java.io.Serializable;

/**
 * Created by mibanez
 */
public class MetricEvent implements Serializable {

    private String name;
    private Class clazz;
    private Object value;

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
