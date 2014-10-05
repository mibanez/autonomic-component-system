package cl.niclabs.scada.acs.component.controllers.monitoring;

import cl.niclabs.scada.acs.component.controllers.*;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.RecordStore;
import cl.niclabs.scada.acs.component.controllers.utils.ValidWrapper;
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
import java.util.Map;

public class MonitoringControllerImpl extends AbstractPAComponentController
        implements MonitoringController, ACSEventListener, LifeCycleController, BindingController {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringController.class);
    private MetricEventListener metricEventListener;

    private final Map<String, Metric> metrics = new HashMap<>();
    private final RecordStore recordStore = new RecordStore();
    private PAEventListener eventListener;

    @Override
    @SuppressWarnings("unchecked")
    public Metric add(String metricId, String className) throws DuplicatedElementIdException, InvalidElementException {
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
    public <METRIC extends Metric> Metric add(String metricId, Class<METRIC> clazz)
            throws DuplicatedElementIdException, InvalidElementException {

        try {
            if (metrics.containsKey(metricId)) {
                throw new DuplicatedElementIdException("The metric id " + metricId + " already exists!");
            }
            metrics.put(metricId, clazz.newInstance());
            return new RepresentativeMetric(metricId, this);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new InvalidElementException(e);
        }
    }

    @Override
    public Metric get(String id) throws ElementNotFoundException {
        if (metrics.containsKey(id)) {
            return new RepresentativeMetric(id, this);
        }
        throw new ElementNotFoundException(notFoundMessage(id));
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
    public String[] getRegisteredIds() {
        return metrics.keySet().toArray(new String[metrics.size()]);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <TYPE extends Serializable> Wrapper<TYPE> getValue(String id) {
        if ( ! metrics.containsKey(id) ) {
            return new WrongWrapper<>(new ElementNotFoundException(notFoundMessage(id)));
        } else try {
            return new ValidWrapper<>((TYPE) metrics.get(id).getValue());
        } catch (CommunicationException e) {
            return new WrongWrapper<>(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <VALUE extends Serializable> Wrapper<VALUE> measure(String id) {
        if ( ! metrics.containsKey(id) ) {
            return new WrongWrapper<>(new ElementNotFoundException(notFoundMessage(id)));
        } else try {
            Metric metric = metrics.get(id);
            metric.measure(recordStore);
            metricEventListener.notifyUpdate(new MetricEvent(id, metric));
            return new ValidWrapper<>((VALUE) metric.getValue());
        } catch (CommunicationException e) {
            return new WrongWrapper<>(e);
        }

    }

    @Override
    public Wrapper<Boolean> isEnabled(String id) {
        if ( ! metrics.containsKey(id) ) {
            return new WrongWrapper<>(new ElementNotFoundException(notFoundMessage(id)));
        } else try {
            return new ValidWrapper<>(metrics.get(id).isEnabled());
        } catch (CommunicationException e) {
            return new WrongWrapper<>(e);
        }
    }

    @Override
    public Wrapper<Boolean> setEnabled(String id, boolean enabled) {
        if ( ! metrics.containsKey(id) ) {
            return new WrongWrapper<>(new ElementNotFoundException(notFoundMessage(id)));
        } else try {
            metrics.get(id).setEnabled(enabled);
            return new ValidWrapper<>(true);
        } catch (CommunicationException e) {
            return new WrongWrapper<>(e);
        }
    }

    @Override
    public Wrapper<Boolean> subscribeTo(String id, ACSEventType eventType) throws CommunicationException {
        if ( ! metrics.containsKey(id) ) {
            return new WrongWrapper<>(new ElementNotFoundException(notFoundMessage(id)));
        } else try {
            metrics.get(id).subscribeTo(eventType);
            return new ValidWrapper<>(true);
        } catch (CommunicationException e) {
            return new WrongWrapper<>(e);
        }
    }

    @Override
    public Wrapper<Boolean> unsubscribeFrom(String id, ACSEventType eventType) throws CommunicationException {
        if ( ! metrics.containsKey(id) ) {
            return new WrongWrapper<>(new ElementNotFoundException(notFoundMessage(id)));
        } else try {
            metrics.get(id).unsubscribeFrom(eventType);
            return new ValidWrapper<>(true);
        } catch (CommunicationException e) {
            return new WrongWrapper<>(e);
        }
    }

    @Override
    public Wrapper<Boolean> isSubscribedTo(String id, ACSEventType eventType) throws CommunicationException {
        if ( ! metrics.containsKey(id) ) {
            return new WrongWrapper<>(new ElementNotFoundException(notFoundMessage(id)));
        } else try {
            return new ValidWrapper<>(metrics.get(id).isEnabled());
        } catch (CommunicationException e) {
            return new WrongWrapper<>(e);
        }
    }

    @Override
    public void notifyACSEvent(ACSEventType eventType) {
        for (Map.Entry<String, Metric> entry : metrics.entrySet()) {
            try {
                if (entry.getValue().isSubscribedTo(eventType)) {
                    entry.getValue().measure(recordStore);
                    metricEventListener.notifyUpdate(new MetricEvent(entry.getKey(), entry.getValue()));
                }
            } catch (CommunicationException e) {
                logger.warn("Exceptions during metric measuring. Metric={} Event={} ExceptionMessage=",
                        entry.getKey(), eventType, e.getMessage());
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
