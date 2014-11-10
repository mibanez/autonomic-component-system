package cl.niclabs.scada.acs.component.controllers;

import cl.niclabs.scada.acs.component.ACSUtils;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;

import java.util.HashSet;

/**
 * Created by mibanez
 */
public class RuleProxy extends GenericElementProxy {

    private final AnalysisController analysisController;

    public RuleProxy(String id, Component host) throws CommunicationException {
        super(id, host);

        try {
            analysisController = ACSUtils.getAnalysisController(host);
            if (analysisController == null) {
                throw new NullPointerException("AnalysisController not found");
            }
        } catch (NoSuchInterfaceException e) {
            throw new CommunicationException(e);
        }
    }

    public ACSAlarm getAlarm() throws CommunicationException {
        return analysisController.getAlarm(getId()).unwrap();
    }

    public ACSAlarm verify() throws CommunicationException {
        return analysisController.verify(getId()).unwrap();
    }

    // SUBSCRIPTION

    public void subscribeTo(String metricId) throws CommunicationException {
        analysisController.subscribeTo(getId(), metricId).unwrap();
    }

    public void unsubscribeFrom(String metricId) throws CommunicationException {
        analysisController.subscribeTo(getId(), metricId).unwrap();
    }

    public boolean isSubscribedTo(String metricId) throws CommunicationException {
        return analysisController.subscribeTo(getId(), metricId).unwrap();
    }

    public HashSet<String> getSubscriptions() throws CommunicationException {
        return analysisController.getSubscriptions(getId()).unwrap();
    }

    // GENERIC ELEMENT

    public boolean isEnabled() throws CommunicationException {
        return analysisController.isEnabled(getId()).unwrap();
    }

    public void setEnabled(boolean enabled) throws CommunicationException {
        analysisController.setEnabled(getId(), enabled).unwrap();
    }

    // PROXY

    public AnalysisController getAnalysisController() {
        return analysisController;
    }

}
