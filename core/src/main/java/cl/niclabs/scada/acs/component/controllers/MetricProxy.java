package cl.niclabs.scada.acs.component.controllers;

import cl.niclabs.scada.acs.component.ACSUtils;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.monitoring.RecordEvent;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;

import java.io.Serializable;

/**
 * Created by mibanez
 */
public class MetricProxy<TYPE extends Serializable> extends GenericElementProxy {

    private final MonitoringController monitoringController;


    public MetricProxy(String id, Component host) throws CommunicationException {
        super(id, host);
        try {
            this.monitoringController = ACSUtils.getMonitoringController(host);
        } catch (NoSuchInterfaceException e) {
            throw new CommunicationException(e);
        }
    }

    public TYPE getValue() throws CommunicationException {
        return (TYPE) monitoringController.getValue(getId()).unwrap();
    }

    public TYPE measure() throws CommunicationException {
        return (TYPE) monitoringController.calculate(getId()).unwrap();
    }

    // SUBSCRIPTION

    public void subscribeTo(RecordEvent eventType) throws CommunicationException {
        monitoringController.subscribeTo(getId(), eventType).unwrap();
    }

    public void unsubscribeFrom(RecordEvent eventType) throws CommunicationException {
        monitoringController.unsubscribeFrom(getId(), eventType).unwrap();
    }

    public boolean isSubscribedTo(RecordEvent eventType) throws CommunicationException {
        return monitoringController.isSubscribedTo(getId(), eventType).unwrap();
    }

    // GENERIC ELEMENT

    public void setEnabled(boolean enabled) throws CommunicationException {
        monitoringController.setEnabled(getId(), enabled).unwrap();
    }

    public boolean isEnabled() throws CommunicationException {
        return monitoringController.isEnabled(getId()).unwrap();
    }

    // PROXY

    public MonitoringController getMonitoringController() {
        return monitoringController;
    }

}
