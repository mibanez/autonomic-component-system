package cl.niclabs.scada.acs.component.controllers.monitoring;

import cl.niclabs.scada.acs.component.controllers.MonitorController;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.RecordStore;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.proactive.core.component.componentcontroller.AbstractPAComponentController;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MonitorControllerImpl extends AbstractPAComponentController
        implements MonitorController, MonitorNotifier, LifeCycleController {

    private final Map<String, Metric> metrics = new HashMap<>();
    private final RecordStore recordStore = new RecordStore();
    private ACSEventListener eventListener;


    @Override
    @SuppressWarnings("unchecked")
    public Wrapper<Boolean> addMetric(String name, String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (Metric.class.isAssignableFrom(clazz)) {
                return addMetric(name, (Class<Metric>) clazz);
            }
            return new Wrapper<>(false, "Metric class is not assignable from the found class " + clazz.getName());
        }
        catch (Exception e) {
            return new Wrapper<>(false, "Fail to get metric class " + className, e);
        }
    }

    @Override
    public <METRIC extends Metric> Wrapper<Boolean> addMetric(String name, Class<METRIC> clazz) {
        try {
            if (metrics.containsKey(name)) {
                return new Wrapper<>(false, "The metric name " + name + " already exists");
            }
            metrics.put(name, clazz.newInstance());
            return new Wrapper<>(true, "Metric " + name + " added correctly");
        }
        catch (Exception e) {
            return new Wrapper<>(false, "Fail to instantiate metric " + clazz.getName(), e);
        }
    }

    @Override
    public Wrapper<Boolean> removeMetric(String name) {
        if (metrics.containsKey(name)) {
            metrics.remove(name);
            return new Wrapper<>(true);
        }
        return new Wrapper<>(false, String.format("no metric with name %s found", name));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <VALUE extends Serializable> Wrapper<VALUE> getValue(String name) {
        if (metrics.containsKey(name)) {
            return metrics.get(name).getWrappedValue();
        }
        return new Wrapper(null, String.format("no metric found with name %s", name));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <VALUE extends Serializable> Wrapper<VALUE> measure(String name) {
        if (metrics.containsKey(name)) {
            Metric metric = metrics.get(name);
            metric.measure(recordStore);
            return metric.getWrappedValue();
        }
        return new Wrapper<>(null, String.format("no metric with name %s found", name));
    }

    @Override
    public Wrapper<String[]> getMetricNames() {
        return new Wrapper<>(metrics.keySet().toArray(new String[metrics.size()]));
    }

    @Override
    public void notifyACSEvent(ACSEventType eventType) {
        for (Metric metric : metrics.values()) {
            if (metric.isSubscribedTo(eventType)) {
                metric.measure(recordStore);
            }
        }
    }

    @Override
    public String getFcState() {
        return null; // never used on
    }

    @Override
    public void startFc() throws IllegalLifeCycleException {
        if (eventListener == null) {
            String runtimeURL = ProActiveRuntimeImpl.getProActiveRuntime().getURL();
            eventListener = new ACSEventListener(this, recordStore, hostComponent.getID(), runtimeURL);
        }
    }

    @Override
    public void stopFc() throws IllegalLifeCycleException {
        // nothing
    }

}
