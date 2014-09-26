package cl.niclabs.scada.acs.component.controllers.monitoring;

/**
 * Created by mibanez
 */
public interface MonitorNotifier {

    public void notifyACSEvent(ACSEventType eventType);

}
