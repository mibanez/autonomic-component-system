package cl.niclabs.scada.acs.component.controllers.planning;

import cl.niclabs.scada.acs.component.controllers.GenericElement;
import cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm;
import cl.niclabs.scada.acs.component.controllers.execution.ExecutionController;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;

import java.util.HashSet;

/**
 * Plan
 *
 */
public abstract class Plan extends GenericElement {

    private final HashSet<String> subscriptions = new HashSet<>();

    public abstract void doPlanFor(String ruleName, ACSAlarm alarm, MonitoringController monitorCtrl,
            ExecutionController executionCtrl);

    
    // SUBSCRIPTIONS

    public void subscribeTo(String ruleId) {
        subscriptions.add(ruleId);
    }

    public void unsubscribeFrom(String ruleId) {
        subscriptions.remove(ruleId);
    }

    public HashSet<String> getSubscriptions() {
        return new HashSet<>(subscriptions);
    }

    public boolean isSubscribedTo(String ruleId) {
        return subscriptions.contains(ruleId);
    }

}
