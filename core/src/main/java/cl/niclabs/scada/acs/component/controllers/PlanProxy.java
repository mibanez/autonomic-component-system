package cl.niclabs.scada.acs.component.controllers;

import cl.niclabs.scada.acs.component.ACSUtils;
import cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm;
import cl.niclabs.scada.acs.component.controllers.planning.PlanningController;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;

import java.util.HashSet;

/**
 * Created by mibanez
 */
public class PlanProxy extends GenericElementProxy {

    private final PlanningController planningController;


    public PlanProxy(String id, Component host) throws CommunicationException {
        super(id, host);

        try {
            planningController = ACSUtils.getPlanningController(host);
        } catch (NoSuchInterfaceException e) {
            throw new CommunicationException(e);
        }
    }

    public void doPlanFor(String ruleId, ACSAlarm alarm) throws CommunicationException {
        planningController.doPlanFor(getId(), ruleId, alarm).unwrap();
    }

    // SUBSCRIPTION

    public void globallySubscribe(ACSAlarm alarm) throws CommunicationException {
        planningController.globallySubscribe(getId(), alarm).unwrap();
    }

    public void globallyUnsubscribe() throws CommunicationException {
        planningController.globallyUnsubscribe(getId()).unwrap();
    }

    public ACSAlarm getGlobalSubscription() throws CommunicationException {
        return planningController.getGlobalSubscription(getId()).unwrap();
    }

    public void subscribeTo(String ruleId, ACSAlarm alarmLevel) throws CommunicationException {
        planningController.subscribeTo(getId(), ruleId, alarmLevel).unwrap();
    }

    public void unsubscribeFrom(String ruleId) throws CommunicationException {
        planningController.unsubscribeFrom(getId(), ruleId).unwrap();
    }

    public HashSet<PlanningController.AlarmSubscription> getSubscriptions() throws CommunicationException {
        return planningController.getSubscriptions(getId()).unwrap();
    }

    public boolean isSubscribedTo(String ruleId, ACSAlarm alarmLevel) throws CommunicationException {
        return planningController.isSubscribedTo(getId(), ruleId, alarmLevel).unwrap();
    }

    // GENERIC ELEMENT

    public boolean isEnabled() throws CommunicationException {
        return planningController.isEnabled(getId()).unwrap();
    }

    public void setEnabled(boolean enabled) throws CommunicationException {
        planningController.setEnabled(getId(), enabled).unwrap();
    }

    // PROXY

    public PlanningController getPlanningController() {
        return planningController;
    }

}
