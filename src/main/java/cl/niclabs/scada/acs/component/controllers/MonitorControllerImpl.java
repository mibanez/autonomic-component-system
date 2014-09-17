package cl.niclabs.scada.acs.component.controllers;

import cl.niclabs.scada.acs.component.controllers.monitoring.Metric;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import org.objectweb.proactive.core.component.componentcontroller.AbstractPAComponentController;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MonitorControllerImpl extends AbstractPAComponentController implements MonitorController {

    private final Map<String, Metric> metrics = new HashMap<>();

    public MonitorControllerImpl() {

    }

    @Override
    public Wrapper<Boolean> addMetric(String name, Metric metric) {
        if (metrics.containsKey(name)) {
            return new Wrapper<>(false, String.format("the metric name %s already exists", name));
        }
        metrics.put(name, metric);
        return new Wrapper<>(true, String.format("metric %s added correctly", name));
    }

    @Override
    public <TYPE extends Serializable> Wrapper<TYPE> getValue(String name) {
        if (metrics.containsKey(name)) {
            return metrics.get(name).getWrappedValue();
        }
        return new Wrapper<>(null, String.format("no metric found with name %s", name));
    }

}
