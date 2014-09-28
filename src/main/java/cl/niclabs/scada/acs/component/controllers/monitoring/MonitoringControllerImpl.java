package cl.niclabs.scada.acs.component.controllers.monitoring;

import cl.niclabs.scada.acs.component.controllers.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.RecordStore;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.proactive.core.component.componentcontroller.AbstractPAComponentController;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MonitoringControllerImpl extends AbstractPAComponentController
        implements MonitoringController, ACSEventListener, LifeCycleController, BindingController {

    public static final String CONTROLLER_NAME = "MonitorController";
    public static final String MONITORING_CONTROLLER_SERVER_ITF = "monitoring-controller-server-itf";
    public static final String METRIC_EVENT_LISTENER_CLIENT_ITF = "metric-event-listener-client-itf-nf";

    private MetricEventListener metricEventListener;

    private final Map<String, Metric> metrics = new HashMap<>();
    private final RecordStore recordStore = new RecordStore();
    private PAEventListener eventListener;

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
            metricEventListener.notifyUpdate(new MetricEvent(name, metric));
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
        for (Map.Entry<String, Metric> entry : metrics.entrySet()) {
            if (entry.getValue().isSubscribedTo(eventType)) {
                entry.getValue().measure(recordStore);
                metricEventListener.notifyUpdate(new MetricEvent(entry.getKey(), entry.getValue()));
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
            eventListener = new PAEventListener(this, recordStore, hostComponent.getID(), runtimeURL);
        }
    }

    @Override
    public void stopFc() throws IllegalLifeCycleException {
        // nothing
    }

    @Override
    public String[] listFc() {
        return new String[] { METRIC_EVENT_LISTENER_CLIENT_ITF };
    }

    @Override
    public Object lookupFc(String name) throws NoSuchInterfaceException {
        switch (name) {
            case METRIC_EVENT_LISTENER_CLIENT_ITF: return metricEventListener;
        }
        throw new NoSuchInterfaceException(name);
    }

    @Override
    public void bindFc(String name, Object o) throws NoSuchInterfaceException {
        switch (name) {
            case METRIC_EVENT_LISTENER_CLIENT_ITF: metricEventListener = (MetricEventListener) o; break;
            default: throw new NoSuchInterfaceException(name);
        }
    }

    @Override
    public void unbindFc(String name) throws NoSuchInterfaceException {
        switch (name) {
            case METRIC_EVENT_LISTENER_CLIENT_ITF: metricEventListener = null; break;
            default: throw new NoSuchInterfaceException(name);
        }
    }
}
