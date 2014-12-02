package cl.niclabs.scada.acs.gcmscript.model;

import cl.niclabs.scada.acs.component.ACSManager;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
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
    private final String metricName;

    public MetricNode(ACSModel model, Component host, String metricName) throws NoSuchInterfaceException {
        super(model.getNodeKind("metric"));
        this.host = host;
        this.monitoringController = ACSManager.getMonitoringController(host);
        this.metricName = metricName;
    }

    public MetricNode(ACSModel model, Component host, MonitoringController monitoringController, String metricName) throws NoSuchInterfaceException {
        super(model.getNodeKind("metric"));
        this.host = host;
        this.monitoringController = monitoringController;
        this.metricName = metricName;
    }

    @Override
    public Object getProperty(String name) {
        switch (name) {
            case "name":    return metricName;
            case "state":   return getState();
            case "value":   return getValue();
            default:        throw new NoSuchElementException("Invalid property name '" + name + "'.");
        }
    }

    @Override
    public void setProperty(String name, Object value) {
        checkSetRequest(name, value);
        switch (name) {
            case "state":   setState((String) value); break;
            default:        throw new NoSuchElementException("Invalid property name '" + name + "'");
        }
    }

    @Override
    public String toString() {
        return String.format("[Metric id=%s]", metricName);
    }

    private Object getValue() {
        return checkWrapper(monitoringController.getValue(metricName));
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

    public String getMetricName() {
        return metricName;
    }

    public String getState() {
        Wrapper<Boolean> result = monitoringController.isEnabled(metricName);
        if (result.isValid()) {
            return result.unwrap() ? "ENABLED" : "DISABLED";
        }
        return "UNKNOWN: " + result.getMessage();
    }

    public void setState(String state) {
        if (state.equals("ENABLED")) {
            monitoringController.setEnabled(metricName, true);
        } else if (state.equals("DISABLED")) {
            monitoringController.setEnabled(metricName, false);
        } else {
            throw new UnsupportedOperationException("Only ENABLED and DISABLED states supported");
        }
    }
}
