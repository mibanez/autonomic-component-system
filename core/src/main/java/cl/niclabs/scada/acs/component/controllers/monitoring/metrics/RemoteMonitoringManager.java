package cl.niclabs.scada.acs.component.controllers.monitoring.metrics;

import static cl.niclabs.scada.acs.component.ACSManager.MONITORING_CONTROLLER;


public interface RemoteMonitoringManager {

    static final String ITF_NAME = "remote-monitoring-manager-nf";

    static final String REMOTE_MONITORING_SUFFIX = String.format("-remote-%s", MONITORING_CONTROLLER);
    static final String INTERNAL_MONITORING_SUFFIX = String.format("-remote-%s-nf", MONITORING_CONTROLLER);

    void remoteInit();

    void remoteStop();
}
