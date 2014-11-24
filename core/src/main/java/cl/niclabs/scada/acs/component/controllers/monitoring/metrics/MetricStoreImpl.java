package cl.niclabs.scada.acs.component.controllers.monitoring.metrics;

import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.monitoring.events.RecordEvent;
import cl.niclabs.scada.acs.component.controllers.monitoring.events.RecordEventListener;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.RecordStore;
import cl.niclabs.scada.acs.component.controllers.utils.ValidWrapper;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.component.controllers.utils.WrongWrapper;
import cl.niclabs.scada.acs.gcmscript.CommunicationException;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.componentcontroller.AbstractPAComponentController;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/*
@DefineGroups({
        @Group(name = "ImmediateMonitoring", selfCompatible = true),
        @Group(name = "Monitoring", selfCompatible = false)
})
@DefineRules({
        @Compatible(value = { "ImmediateMonitoring", "Monitoring" })
})*/
public class MetricStoreImpl extends AbstractPAComponentController implements MetricStore, RecordEventListener,
        RemoteMonitoringManager, BindingController {

    private final Map<String, Metric> metrics = new HashMap<>();

    private MetricEventListener metricEventListener;
    private RecordStore recordStore;

    private Map<String, MonitoringController> remoteMonitoring = new HashMap<>();

    @Override
    //@MemberOf("Monitoring")
    public void remoteInit() {
        for (MonitoringController remoteMonitoringController : remoteMonitoring.values()) {
            remoteMonitoringController.startMonitoring();
        }
    }

    @Override
    //@MemberOf("Monitoring")
    public void remoteStop() {
        for (MonitoringController remoteMonitoringController : remoteMonitoring.values()) {
            remoteMonitoringController.startMonitoring();
        }
    }

    @Override
    //@MemberOf("Monitoring")
    public <METRIC extends Metric> Wrapper<Boolean> add(String id, Class<METRIC> clazz) {
        if (metrics.containsKey(id)) {
            return new ValidWrapper<>(false, "key already registered");
        }
        try {
            metrics.put(id, clazz.newInstance());
            return new ValidWrapper<>(true);
        } catch (InstantiationException | IllegalAccessException e) {
            return new ValidWrapper<>(false, e.getMessage());
        }
    }

    @Override
    //@MemberOf("Monitoring")
    public Wrapper<Boolean> remove(String id) {
        if (metrics.containsKey(id)) {
            metrics.remove(id);
            return new ValidWrapper<>(true);
        }
        return new ValidWrapper<>(false, "id not registered");
    }

    @Override
    //@MemberOf("Monitoring")
    public ArrayList<String> getRegisteredIds() {
        return new ArrayList<>(metrics.keySet());
    }

    @Override
    //@MemberOf("Monitoring")
    public <VALUE extends Serializable> Wrapper<VALUE> getValue(String id) {
        if (metrics.containsKey(id)) {
            return new ValidWrapper<>((VALUE) metrics.get(id).getValue());
        }
        return new WrongWrapper<>("id not found: " + id);
    }

    @Override
    //@MemberOf("ImmediateMonitoring")
    public <VALUE extends Serializable> Wrapper<VALUE> getValue(String metricId, String... itfNames) {

        if (itfNames.length == 0) {
            return getValue(metricId);
        }

        MonitoringController mon = remoteMonitoring.get(itfNames[0]);
        if (mon != null) {
            return mon.getValue(metricId, Arrays.copyOfRange(itfNames, 1, itfNames.length));
        }

        return new WrongWrapper<>(String.format("No remote monitoring for interface %s", itfNames[0]));
    }

    @Override
    //@MemberOf("ImmediateMonitoring")
    public <VALUE extends Serializable> Wrapper<VALUE> calculate(String metricId) {
        if (metrics.containsKey(metricId)) {
            Metric metric = metrics.get(metricId);
            VALUE value = (VALUE) metric.calculate(recordStore, this);
            metricEventListener.notifyMetricChange(metricId);
            return new ValidWrapper<>(value);
        }
        return new WrongWrapper<>("id not found: " + metricId);
    }

    @Override
    //@MemberOf("Monitoring")
    public Wrapper<Boolean> isEnabled(String metricId) {
        if (metrics.containsKey(metricId)) {
            return new ValidWrapper<>(metrics.get(metricId).isEnabled());
        }
        return new WrongWrapper<>("id not found: " + metricId);
    }

    @Override
    //@MemberOf("Monitoring")
    public Wrapper<Boolean> setEnabled(String metricId, boolean enabled) {
        if (metrics.containsKey(metricId)) {
            metrics.get(metricId).setEnabled(enabled);
            return new ValidWrapper<>(true);
        }
        return new WrongWrapper<>("id not found: " + metricId);
    }

    @Override
    //@MemberOf("Monitoring")
    public Wrapper<Boolean> subscribeTo(String id, RecordEvent eventType) throws CommunicationException {
        if (metrics.containsKey(id)) {
            metrics.get(id).subscribeTo(eventType);
            return new ValidWrapper<>(true);
        }
        return new WrongWrapper<>("id not found: " + id);
    }

    @Override
    //@MemberOf("Monitoring")
    public Wrapper<Boolean> unsubscribeFrom(String id, RecordEvent eventType) throws CommunicationException {
        if (metrics.containsKey(id)) {
            metrics.get(id).unsubscribeFrom(eventType);
            return new ValidWrapper<>(true);
        }
        return new WrongWrapper<>("id not found: " + id);
    }

    @Override
    //@MemberOf("Monitoring")
    public Wrapper<Boolean> isSubscribedTo(String id, RecordEvent eventType) throws CommunicationException {
        if (metrics.containsKey(id)) {
            return new ValidWrapper<>(metrics.get(id).isSubscribedTo(eventType));
        }
        return new WrongWrapper<>("id not found: " + id);
    }

    @Override
    //@MemberOf("Monitoring")
    public void notifyRecordEvent(RecordEvent eventType) {

        for (Map.Entry<String, Metric> entry : metrics.entrySet()) {
            if (entry.getValue().isSubscribedTo(eventType)) {
                entry.getValue().calculate(recordStore, this);
                metricEventListener.notifyMetricChange(entry.getKey());
            }
        }
    }

    @Override
    //@MemberOf("Monitoring")
    public String[] listFc() {
        ArrayList<String> list = new ArrayList<>();
        for (InterfaceType itfType : hostComponent.getComponentParameters().getClientInterfaceTypes()) {
            list.add(itfType.getFcItfName().concat(INTERNAL_MONITORING_SUFFIX));
        }
        if (hostComponent.getComponentParameters().getHierarchicalType().equals(Constants.COMPOSITE)) {
            for (InterfaceType itfType : hostComponent.getComponentParameters().getServerInterfaceTypes()) {
                list.add(itfType.getFcItfName().concat(INTERNAL_MONITORING_SUFFIX));
            }
        }
        list.add(MetricEventListener.ITF_NAME);
        list.add(RecordStore.ITF_NAME);
        return list.toArray(new String[list.size()]);
    }

    @Override
    //@MemberOf("Monitoring")
    public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
        if (clientItfName.endsWith(INTERNAL_MONITORING_SUFFIX)) {
            String realName = clientItfName.substring(0, clientItfName.lastIndexOf(INTERNAL_MONITORING_SUFFIX));
            return remoteMonitoring.get(realName);
        } else if (clientItfName.equals(MetricEventListener.ITF_NAME)) {
            return metricEventListener;
        } else if (clientItfName.equals(RecordStore.ITF_NAME)) {
            return recordStore;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    @Override
    //@MemberOf("Monitoring")
    public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
        if (clientItfName.endsWith(INTERNAL_MONITORING_SUFFIX)) {
            String realName = clientItfName.substring(0, clientItfName.lastIndexOf(INTERNAL_MONITORING_SUFFIX));
            remoteMonitoring.put(realName, (MonitoringController) serverItf);
        } else if (clientItfName.equals(MetricEventListener.ITF_NAME)) {
            metricEventListener = (MetricEventListener) serverItf;
        } else if (clientItfName.equals(RecordStore.ITF_NAME)) {
            recordStore = (RecordStore) serverItf;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    @Override
    //@MemberOf("Monitoring")
    public void unbindFc(String clientItfName) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
        if (clientItfName.endsWith(INTERNAL_MONITORING_SUFFIX)) {
            String realName = clientItfName.substring(0, clientItfName.lastIndexOf(INTERNAL_MONITORING_SUFFIX));
            remoteMonitoring.remove(realName);
        } else if (clientItfName.equals(MetricEventListener.ITF_NAME)) {
            metricEventListener = null;
        } else if (clientItfName.equals(RecordStore.ITF_NAME)) {
            recordStore = null;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }
}
