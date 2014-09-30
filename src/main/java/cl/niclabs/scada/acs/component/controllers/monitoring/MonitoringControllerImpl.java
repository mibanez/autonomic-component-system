package cl.niclabs.scada.acs.component.controllers.monitoring;

import cl.niclabs.scada.acs.component.controllers.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.RecordStore;
import cl.niclabs.scada.acs.component.controllers.utils.ValidWrapper;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.component.controllers.utils.WrongWrapper;
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
    public Wrapper<Boolean> add(String metricId, String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (Metric.class.isAssignableFrom(clazz)) {
                return add(metricId, (Class<Metric>) clazz);
            }
            throw new ClassCastException("Can't cast " + clazz.getName() + " to " + Metric.class.getName());
        } catch (Exception e) {
            return new WrongWrapper<>("Fail to get metric class from name " + className, e);
        }
    }

    @Override
    public <METRIC extends Metric> Wrapper<Boolean> add(String metricId, Class<METRIC> clazz) {
        try {
            if (!metrics.containsKey(metricId)) {
                metrics.put(metricId, clazz.newInstance());
                return new ValidWrapper<>(true);
            }
            return new ValidWrapper<>(false, "The metric id " + metricId + " already exists");
        }
        catch (Exception e) {
            return new WrongWrapper<>("Fail to instantiate metric form class " + clazz.getName(), e);
        }
    }

    @Override
    public Wrapper<Boolean> remove(String metricId) {
        if (metrics.containsKey(metricId)) {
            metrics.remove(metricId);
            return new ValidWrapper<>(true);
        }
        return new ValidWrapper<>(false, "No metric found with id " + metricId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <VALUE extends Serializable> Wrapper<VALUE> getValue(String metricId) {
        if (metrics.containsKey(metricId)) {
            return new ValidWrapper<>((VALUE) metrics.get(metricId).getValue());
        }
        return new WrongWrapper<>("No metric found with id " + metricId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <VALUE extends Serializable> Wrapper<VALUE> measure(String metricId) {
        if (metrics.containsKey(metricId)) {
            Metric metric = metrics.get(metricId);
            metric.measure(recordStore);
            metricEventListener.notifyUpdate(new MetricEvent(metricId, metric));
            return new ValidWrapper<>((VALUE) metric.getValue());
        }
        return new WrongWrapper<>("No metric found with id " + metricId);
    }

    @Override
    public Wrapper<Boolean> isEnabled(String metricId) {
        if (metrics.containsKey(metricId)) {
            return new ValidWrapper<>(metrics.get(metricId).isEnabled());
        }
        return new WrongWrapper<>("No metric found with id " + metricId);
    }

    @Override
    public Wrapper<Boolean> enable(String metricId) {
        if (metrics.containsKey(metricId)) {
            metrics.get(metricId).enable();
            return new ValidWrapper<>(true);
        }
        return new WrongWrapper<>("No metric found with id " + metricId);
    }

    @Override
    public Wrapper<Boolean> disable(String metricId) {
        if (metrics.containsKey(metricId)) {
            metrics.get(metricId).disable();
            return new ValidWrapper<>(true);
        }
        return new WrongWrapper<>("No metric found with id " + metricId);
    }

    @Override
    public Wrapper<String[]> getRegisteredIds() {
        return new ValidWrapper<>(metrics.keySet().toArray(new String[metrics.size()]));
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
