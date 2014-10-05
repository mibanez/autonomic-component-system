package cl.niclabs.scada.acs.component.controllers.planning;

import cl.niclabs.scada.acs.component.controllers.*;

/**
 * Created by mibanez
 */
public class RepresentativePlan extends Plan {

    private final PlanningController planningController;
    private final String id;

    RepresentativePlan(String id, PlanningController planningController) {
        this.planningController = planningController;
        this.id = id;
    }

    @Override
    public void doPlanFor(String ruleId, ACSAlarm alarm, MonitoringController monitorController) throws CommunicationException {
        planningController.doPlanFor(id, ruleId, alarm).unwrap();
    }

    @Override
    public void globalSubscription(ACSAlarm alarmLevel) throws CommunicationException {
        planningController.globalSubscription(id, alarmLevel).unwrap();
    }

    @Override
    public void subscribeTo(String ruleId, ACSAlarm alarmLevel) throws CommunicationException {
        planningController.subscribeTo(id, ruleId, alarmLevel).unwrap();
    }

    @Override
    public void unsubscribeFrom(String ruleId) throws CommunicationException {
        planningController.unsubscribeFrom(id, ruleId).unwrap();
    }

    @Override
    public boolean isSubscribedTo(String ruleId, ACSAlarm alarmLevel) throws CommunicationException {
        return planningController.isSubscribedTo(id, ruleId, alarmLevel).unwrap();
    }

}
