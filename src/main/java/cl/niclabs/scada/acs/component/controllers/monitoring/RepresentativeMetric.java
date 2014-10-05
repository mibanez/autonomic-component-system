package cl.niclabs.scada.acs.component.controllers.monitoring;

import cl.niclabs.scada.acs.component.controllers.CommunicationException;
import cl.niclabs.scada.acs.component.controllers.Metric;
import cl.niclabs.scada.acs.component.controllers.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.RecordStore;

import java.io.Serializable;

/**
 * Created by mibanez
 */
public class RepresentativeMetric<TYPE extends Serializable> extends Metric<TYPE> {

    private final MonitoringController monitoringController;
    private final String id;

    RepresentativeMetric(String id, MonitoringController monitoringController) {
        this.monitoringController = monitoringController;
        this.id = id;
    }

    @Override
    public void subscribeTo(ACSEventType eventType) throws CommunicationException {
        monitoringController.subscribeTo(id, eventType).unwrap();
    }

    @Override
    public void unsubscribeFrom(ACSEventType eventType) throws CommunicationException {
        monitoringController.unsubscribeFrom(id, eventType).unwrap();
    }

    @Override
    public boolean isSubscribedTo(ACSEventType eventType) throws CommunicationException {
        return monitoringController.isSubscribedTo(id, eventType).unwrap();
    }

    @Override
    public TYPE getValue() throws CommunicationException {
        return (TYPE) monitoringController.getValue(id).unwrap();
    }

    @Override
    public TYPE measure(RecordStore recordStore) throws CommunicationException {
        return (TYPE) monitoringController.measure(id).unwrap();
    }

    @Override
    public void setEnabled(boolean enabled) throws CommunicationException {
        monitoringController.setEnabled(id, enabled).unwrap();
    }

    @Override
    public boolean isEnabled() throws CommunicationException {
        return monitoringController.isEnabled(id).unwrap();
    }

    public MonitoringController getMonitoringController() {
        return monitoringController;
    }

    public String getId() {
        return id;
    }
}
