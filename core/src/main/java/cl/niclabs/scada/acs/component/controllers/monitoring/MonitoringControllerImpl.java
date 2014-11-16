package cl.niclabs.scada.acs.component.controllers.monitoring;

import cl.niclabs.scada.acs.component.controllers.DuplicatedElementIdException;
import cl.niclabs.scada.acs.component.controllers.ElementNotFoundException;
import cl.niclabs.scada.acs.component.controllers.InvalidElementException;
import cl.niclabs.scada.acs.component.controllers.monitoring.events.GCMPAEventListener;
import cl.niclabs.scada.acs.component.controllers.monitoring.events.RecordEvent;
import cl.niclabs.scada.acs.component.controllers.monitoring.events.RecordEventListener;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.RecordStore;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.RecordStoreImpl;
import cl.niclabs.scada.acs.component.controllers.utils.ValidWrapper;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.component.controllers.utils.WrongWrapper;
import cl.niclabs.scada.acs.gcmscript.CommunicationException;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.proactive.core.component.componentcontroller.AbstractPAComponentController;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MonitoringControllerImpl extends AbstractPAComponentController implements MonitoringController,
        RecordEventListener, LifeCycleController, BindingController {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringController.class);
    private MetricEventListener metricEventListener;

    private final Map<String, Metric> metrics = new HashMap<>();
    private final RecordStore recordStore = new RecordStoreImpl();
    private GCMPAEventListener eventListener;

    @Override
    @SuppressWarnings("unchecked")
    public void add(String metricId, String className) throws DuplicatedElementIdException, InvalidElementException {
        try {
            Class<?> clazz = Class.forName(className);
            if (!Metric.class.isAssignableFrom(clazz)) {
                String msg = String.format("Can't cast %s to $s", clazz.getName(), Metric.class.getName());
                throw new InvalidElementException(msg);
            }
            add(metricId, (Class<Metric>) clazz);
        } catch (ClassNotFoundException e) {
            String msg = String.format("Can't found class %s", className);
            throw new InvalidElementException(msg, e);
        }
    }

    @Override
    public <METRIC extends Metric> void add(String metricId, Class<METRIC> clazz)
            throws DuplicatedElementIdException, InvalidElementException {
        if (metrics.containsKey(metricId)) {
            String msg = String.format("The metric id %s is already registered", metricId);
            throw new DuplicatedElementIdException(msg);
        } else try {
            metrics.put(metricId, clazz.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new InvalidElementException(e);
        }
    }

    @Override
    public void remove(String id) throws ElementNotFoundException {
        if (!metrics.containsKey(id)) {
            throw new ElementNotFoundException(notFoundMessage(id));
        }
        metrics.remove(id);
    }

    @Override
    public ArrayList<String> getRegisteredIds() {
        return new ArrayList<>(metrics.keySet());
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
            metric.calculate(recordStore);
            metricEventListener.notifyUpdate(new MetricEvent(id, metric));
            return new ValidWrapper<>((VALUE) metric.getValue());
        }
        return new WrongWrapper<>(notFoundMessage(id));
    }

    @Override
    public WrongWrapper<MonitoringController> remoteMonitoring(String interfaceName) {
        return new WrongWrapper<>(String.format("No remote monitoring for interface %s", interfaceName));
    }

    @Override
    public void notifyACSEvent(RecordEvent eventType) {
        for (Map.Entry<String, Metric> entry : metrics.entrySet()) {
            if (entry.getValue().isSubscribedTo(eventType)) {
                entry.getValue().calculate(recordStore);
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
            eventListener = new GCMPAEventListener(this, recordStore, hostComponent.getID(), runtimeURL);
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
