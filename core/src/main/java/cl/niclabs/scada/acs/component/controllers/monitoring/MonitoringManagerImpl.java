package cl.niclabs.scada.acs.component.controllers.monitoring;

import cl.niclabs.scada.acs.component.controllers.InvalidElementException;
import cl.niclabs.scada.acs.component.controllers.monitoring.events.GCMPAEventListener;
import cl.niclabs.scada.acs.component.controllers.monitoring.events.RecordEvent;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.Metric;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.MetricStore;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.RemoteMonitoringManager;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.gcmscript.model.CommunicationException;
import org.apache.log4j.Logger;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.proactive.core.component.componentcontroller.AbstractPAComponentController;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

import java.io.Serializable;
import java.util.ArrayList;
/*
@DefineGroups({
        @Group(name = "ImmediateMonitoring", selfCompatible = true),
        @Group(name = "Monitoring", selfCompatible = false)
})
@DefineRules({
        @Compatible(value = { "ImmediateMonitoring", "Monitoring" })
})*/
public class MonitoringManagerImpl extends AbstractPAComponentController implements MonitoringController, BindingController {

    private static final Logger logger = ProActiveLogger.getLogger("ACS");

    private RemoteMonitoringManager remoteMonitoringManager;
    private GCMPAEventListener eventListener;
    private MetricStore metricStore;

    private boolean started = false;

    @Override
    //@MemberOf("Monitoring")
    public void startMonitoring() {
        if (started) {
            return;
        }
        eventListener.init(hostComponent.getID(), ProActiveRuntimeImpl.getProActiveRuntime().getURL());
        remoteMonitoringManager.remoteInit();
        started = true;
    }

    @Override
    //@MemberOf("Monitoring")
    public void stopMonitoring() {
        if (started == false) {
            return;
        }
        eventListener.stop();
        remoteMonitoringManager.remoteStop();
        started = false;
    }

    @Override
    @SuppressWarnings("unchecked")
    //@MemberOf("Monitoring")
    public void add(String metricId, String className) throws InvalidElementException {
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
    //@MemberOf("Monitoring")
    public <METRIC extends Metric> Wrapper<Boolean> add(String id, Class<METRIC> clazz) {
        return metricStore.add(id, clazz);
    }

    @Override
    //@MemberOf("Monitoring")
    public Wrapper<Boolean> remove(String metricId) {
        return metricStore.remove(metricId);
    }

    @Override
    //@MemberOf("Monitoring")
    public ArrayList<String> getRegisteredIds() {
        return metricStore.getRegisteredIds();
    }

    @Override
    //@MemberOf("Monitoring")
    public Wrapper<Boolean> isEnabled(String id) {
        return metricStore.isEnabled(id);
    }

    @Override
    //@MemberOf("Monitoring")
    public Wrapper<Boolean> setEnabled(String id, boolean enabled) {
        return metricStore.setEnabled(id, enabled);
    }

    @Override
    //@MemberOf("Monitoring")
    public Wrapper<Boolean> subscribeTo(String id, RecordEvent eventType) throws CommunicationException {
        return metricStore.subscribeTo(id, eventType);
    }

    @Override
    //@MemberOf("Monitoring")
    public Wrapper<Boolean> unsubscribeFrom(String id, RecordEvent eventType) throws CommunicationException {
        return metricStore.unsubscribeFrom(id, eventType);
    }

    @Override
    //@MemberOf("Monitoring")
    public Wrapper<Boolean> isSubscribedTo(String id, RecordEvent eventType) throws CommunicationException {
        return metricStore.isSubscribedTo(id, eventType);
    }

    @Override
    @SuppressWarnings("unchecked")
    //@MemberOf("ImmediateMonitoring")
    public <TYPE extends Serializable> Wrapper<TYPE> getValue(String id) {
        return metricStore.getValue(id);
    }

    @Override
    //@MemberOf("ImmediateMonitoring")
    public <VALUE extends Serializable> Wrapper<VALUE> getValue(String metricId, String... itfNames) {
        return metricStore.getValue(metricId, itfNames);
    }

    @Override
    @SuppressWarnings("unchecked")
    //@MemberOf("Monitoring")
    public <VALUE extends Serializable> Wrapper<VALUE> calculate(String metricId) {
        return metricStore.calculate(metricId);
    }


    // BINDING


    @Override
    //@MemberOf("Monitoring")
    public String[] listFc() {
        return new String[] { MetricStore.ITF_NAME, RemoteMonitoringManager.ITF_NAME, GCMPAEventListener.ITF_NAME };
    }

    @Override
    //@MemberOf("Monitoring")
    public Object lookupFc(String name) throws NoSuchInterfaceException {
        if (name.equals(MetricStore.ITF_NAME)) {
            return metricStore;
        } else if (name.equals(RemoteMonitoringManager.ITF_NAME)) {
            return remoteMonitoringManager;
        } else if (name.equals(GCMPAEventListener.ITF_NAME)) {
            return eventListener;
        } else {
            throw new NoSuchInterfaceException(name);
        }
    }

    @Override
    //@MemberOf("Monitoring")
    public void bindFc(String name, Object itf) throws NoSuchInterfaceException {
        if (name.equals(MetricStore.ITF_NAME)) {
            metricStore = (MetricStore) itf;
        } else if (name.equals(RemoteMonitoringManager.ITF_NAME)) {
            remoteMonitoringManager = (RemoteMonitoringManager) itf;
        } else if (name.equals(GCMPAEventListener.ITF_NAME)) {
            eventListener = (GCMPAEventListener) itf;
        } else {
            throw new NoSuchInterfaceException(name);
        }
    }

    @Override
    //@MemberOf("Monitoring")
    public void unbindFc(String name) throws NoSuchInterfaceException {
        if (name.equals(MetricStore.ITF_NAME)) {
            metricStore = null;
        } else if (name.equals(RemoteMonitoringManager.ITF_NAME)) {
            remoteMonitoringManager = null;
        } else if (name.equals(GCMPAEventListener.ITF_NAME)) {
            eventListener = null;
        } else {
            throw new NoSuchInterfaceException(name);
        }
    }
}
