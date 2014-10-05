package cl.niclabs.scada.acs.component.controllers.analysis;

import cl.niclabs.scada.acs.component.controllers.*;

/**
 * Created by mibanez
 */
public class RepresentativeRule extends Rule {

    private final AnalysisController analysisController;
    private final String id;

    RepresentativeRule(String id, AnalysisController analysisController) {
        this.id = id;
        this.analysisController = analysisController;
    }

    @Override
    public ACSAlarm verify(MonitoringController monitoringController) throws CommunicationException {
        return analysisController.verify(id).unwrap();
    }

    @Override
    public void subscribeTo(String metricId) throws CommunicationException {
        analysisController.subscribeTo(id, metricId).unwrap();
    }

    @Override
    public void unsubscribeFrom(String metricId) throws CommunicationException {
        analysisController.subscribeTo(id, metricId).unwrap();
    }

    @Override
    public boolean isSubscribedTo(String metricId) throws CommunicationException {
        return analysisController.subscribeTo(id, metricId).unwrap();
    }
}
