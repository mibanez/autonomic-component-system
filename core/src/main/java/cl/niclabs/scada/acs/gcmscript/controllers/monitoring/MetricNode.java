package cl.niclabs.scada.acs.gcmscript.controllers.monitoring;

import cl.niclabs.scada.acs.component.ACSUtils;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.gcmscript.ACSModel;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.fscript.model.AbstractNode;

import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * Created by mibanez
 */
public class MetricNode extends AbstractNode {

    private final Component host;
    private final MonitoringController monitoringController;
    private final String metricId;

    public MetricNode(ACSModel model, Component host, String metricId) throws NoSuchInterfaceException {
        super(model.getNodeKind("metric"));
        this.host = host;
        this.monitoringController = ACSUtils.getMonitoringController(host);
        this.metricId = metricId;
    }

    public MetricNode(ACSModel model, Component host, MonitoringController monitoringController, String metricId) throws NoSuchInterfaceException {
        super(model.getNodeKind("metric"));
        this.host = host;
        this.monitoringController = monitoringController;
        this.metricId = metricId;
    }

    @Override
    public Object getProperty(String name) {
        switch (name) {
            case "id":      return metricId;
            case "enabled": return isEnabled();
            case "value":   return getValue();
            default:        throw new NoSuchElementException("Invalid property name '" + name + "'.");
        }
    }

    @Override
    public void setProperty(String name, Object value) {
        checkSetRequest(name, value);
        switch (name) {
            case "enabled": setEnabled((boolean) value); break;
            default:        throw new NoSuchElementException("Invalid property name '" + name + "'");
        }
    }

    @Override
    public String toString() {
        return String.format("[Metric id=%s]", metricId);
    }

    private Object isEnabled() {
        return checkWrapper(monitoringController.isEnabled(metricId));
    }

    private void setEnabled(boolean enabled) {
        checkWrapper(monitoringController.setEnabled(metricId, enabled));
    }

    private Object getValue() {
        return checkWrapper(monitoringController.getValue(metricId));
    }

    private Object checkWrapper(Wrapper<? extends Serializable> wrapper) {
        if (wrapper.isValid()) {
            return wrapper.unwrap();
        }
        throw new NoSuchElementException(wrapper.getMessage());
    }

    public Component getHost() {
        return host;
    }

    public MonitoringController getMonitoringController() {
        return monitoringController;
    }

    public String getMetricId() {
        return metricId;
    }
}
