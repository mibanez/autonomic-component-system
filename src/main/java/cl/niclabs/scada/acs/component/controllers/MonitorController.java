package cl.niclabs.scada.acs.component.controllers;

import cl.niclabs.scada.acs.component.controllers.monitoring.Metric;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;

import java.io.Serializable;

public interface MonitorController {

    /**
     * Adds a new metric to the monitor controller.<br>
     * The name of the metric must be unique.
     * @param name      Name of the new metric
     * @param className Name of the class implementing the metric
     * @return          A wrapped boolean value.<br> True if it was correctly added, False otherwise.
     */
    public Wrapper<Boolean> addMetric(String name, String className);

    /**
     * Adds a new metric to the monitor controller.<br>
     * The name of the metric must be unique.
     * @param name  Name of the new metric
     * @param clazz Class implementing the metric
     * @return      A wrapped boolean value.<br> True if it was correctly added, False otherwise.
     */
    public <METRIC extends Metric> Wrapper<Boolean> addMetric(String name, Class<METRIC> clazz);

    /**
     * Removes a registered metric.
     * @param name  Name of metric to remove
     * @return      A wrapped boolean value.<br> True if it was correctly removed, False otherwise.
     */
    public Wrapper<Boolean> removeMetric(String name);

    /**
     * Returns the value of a metric.
     * @param name  Name of the metric
     * @return      The value of the metric, wrapped
     */
    public <VALUE extends Serializable> Wrapper<VALUE> getValue(String name);

    /**
     * Measures the value of a metric.
     * @param name  Name of the metric to measure
     * @return      The new value of the metric
     */
    public <VALUE extends Serializable> Wrapper<VALUE> measure(String name);

    /**
     * Returns a list with the name of the registered metrics
     * @return  Array with metric names
     */
    public Wrapper<String[]> getMetricNames();

}
