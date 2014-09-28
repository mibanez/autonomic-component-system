package cl.niclabs.scada.acs.component.controllers.monitor;

/**
 * Created by mibanez
 */
public interface MonitorNotifier {

    public void notifyACSEvent(ACSEventType eventType);

}
