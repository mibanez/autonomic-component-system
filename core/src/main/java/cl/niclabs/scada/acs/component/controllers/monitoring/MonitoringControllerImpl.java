package cl.niclabs.scada.acs.component.controllers.monitoring;

import cl.niclabs.scada.acs.component.controllers.*;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.ACSRecordStore;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class MonitoringControllerImpl extends AbstractPAComponentController implements MetricStore,
        MonitoringController, RecordEventListener, LifeCycleController, BindingController {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringController.class);
    private MetricEventListener metricEventListener;

    private final Map<String, Metric> metrics = new HashMap<>();
    private final ACSRecordStore ACSRecordStore = new ACSRecordStore();
    private GCMPAEventListener eventListener;

    @Override
    @SuppressWarnings("unchecked")
    public MetricProxy add(String metricId, String className) throws DuplicatedElementIdException, InvalidElementException {
        try {
            Class<?> clazz = Class.forName(className);
            if (Metric.class.isAssignableFrom(clazz) ) {
                return add(metricId, (Class<Metric>) clazz);
            } else throw new InvalidElementException("Can't cast " + clazz.getName() + " to " + Metric.class.getName());
        } catch (ClassNotFoundException e) {
            throw new InvalidElementException("Class name " + className + " can't be found", e);
        }
    }

    @Override
    public <METRIC extends Metric> MetricProxy add(String metricId, Class<METRIC> clazz)
            throws DuplicatedElementIdException, InvalidElementException {
        if (metrics.containsKey(metricId)) {
            throw new DuplicatedElementIdException("The metric id " + metricId + " already exists!");
        } else try {
            metrics.put(metricId, clazz.newInstance());
            return new MetricProxy(metricId, hostComponent);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new InvalidElementException(e);
        } catch (CommunicationException e) {
            throw new InvalidElementException("Can't create metric proxy: " + e.getMessage());
        }
    }

    @Override
    public void remove(String id) throws ElementNotFoundException {
        if (metrics.containsKey(id)) {
            metrics.remove(id);
        } else {
            throw new ElementNotFoundException(notFoundMessage(id));
        }
    }

    @Override
    public HashSet<String> getRegisteredIds() {
        return new HashSet<>(metrics.keySet());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <TYPE extends Serializable> Wrapper<TYPE> getValue(String id) {
        if (metrics.containsKey(id)) {
            return new ValidWrapper<>((TYPE) metrics.get(id).getValue());
        }
        return new WrongWrapper<>(notFoundMessage(id));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <VALUE extends Serializable> Wrapper<VALUE> calculate(String id) {
        if (metrics.containsKey(id)) {
            Metric metric = metrics.get(id);
            metric.calculate((RecordStore) ACSRecordStore);
            metricEventListener.notifyUpdate(new MetricEvent(id, metric));
            return new ValidWrapper<>((VALUE) metric.getValue());
        }
        return new WrongWrapper<>(notFoundMessage(id));
    }

    @Override
    public Wrapper<Boolean> isEnabled(String id) {
        if (metrics.containsKey(id)) {
            return new ValidWrapper<>(metrics.get(id).isEnabled());
        }
        return new WrongWrapper<>(notFoundMessage(id));
    }

    @Override
    public Wrapper<Boolean> setEnabled(String id, boolean enabled) {
        if (metrics.containsKey(id)) {
            metrics.get(id).setEnabled(enabled);
            return new ValidWrapper<>(true);
        }
        return new WrongWrapper<>(notFoundMessage(id));
    }

    @Override
    public Wrapper<Boolean> subscribeTo(String id, RecordEvent eventType) throws CommunicationException {
        if (metrics.containsKey(id)) {
            metrics.get(id).subscribeTo(eventType);
            return new ValidWrapper<>(true);
        }
        return new WrongWrapper<>(notFoundMessage(id));
    }

    @Override
    public Wrapper<Boolean> unsubscribeFrom(String id, RecordEvent eventType) throws CommunicationException {
        if (metrics.containsKey(id)) {
            metrics.get(id).unsubscribeFrom(eventType);
            return new ValidWrapper<>(true);
        }
        return new WrongWrapper<>(notFoundMessage(id));
    }

    @Override
    public Wrapper<Boolean> isSubscribedTo(String id, RecordEvent eventType) throws CommunicationException {
        if (metrics.containsKey(id)) {
            return new ValidWrapper<>(metrics.get(id).isEnabled());
        }
        return new WrongWrapper<>(notFoundMessage(id));
    }

    @Override
    public void notifyACSEvent(RecordEvent eventType) {
        for (Map.Entry<String, Metric> entry : metrics.entrySet()) {
            if (entry.getValue().isSubscribedTo(eventType)) {
                entry.getValue().calculate(ACSRecordStore);
                metricEventListener.notifyUpdate(new MetricEvent(entry.getKey(), entry.getValue()));
            }
        }
    }

    @Override
    public String getFcState() {
        return null; // never used on ProActive implementation
    }

    @Override
    public void startFc() throws IllegalLifeCycleException {
        if (eventListener == null) {
            String runtimeURL = ProActiveRuntimeImpl.getProActiveRuntime().getURL();
            eventListener = new GCMPAEventListener(this, ACSRecordStore, hostComponent.getID(), runtimeURL);
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

    private String notFoundMessage(String id) {
        return "No metric found with id " + id;
    }

    public static final String CONTROLLER_NAME = "MonitorController";
    public static final String MONITORING_CONTROLLER_SERVER_ITF = "monitoring-controller-server-itf-nf";
    public static final String METRIC_EVENT_LISTENER_CLIENT_ITF = "metric-event-listener-client-itf-nf";

    public static final String EXTERNAL_MONITORING_ITF = "-external-monitoring-controller";
    public static final String INTERNAL_MONITORING_ITF = "-internal-monitoring-controller";
    public static final String INTERNAL_SERVER_MONITORING_ITF = "internal-server-monitoring-controller";

}
