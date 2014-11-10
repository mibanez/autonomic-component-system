package cl.niclabs.scada.acs.component;

import cl.niclabs.scada.acs.component.controllers.AnalysisController;
import cl.niclabs.scada.acs.component.controllers.ExecutionController;
import cl.niclabs.scada.acs.component.controllers.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.PlanningController;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;

/**
 * Utility methods for BuildHelper
 *
 * Created by mibanez
 */
public class ACSUtils {

    public static final String MONITORING_CONTROLLER = "monitor-controller";
    public static final String ANALYSIS_CONTROLLER  = "analysis-controller";
    public static final String PLANNING_CONTROLLER  = "planning-controller";
    public static final String EXECUTION_CONTROLLER = "execution-controller";

    public static MonitoringController getMonitoringController(Component host) throws NoSuchInterfaceException {
        return (MonitoringController) host.getFcInterface(MONITORING_CONTROLLER);
    }

    public static AnalysisController getAnalysisController(Component host) throws NoSuchInterfaceException {
        return (AnalysisController) host.getFcInterface(ANALYSIS_CONTROLLER);
    }

    public static PlanningController getPlanningController(Component host) throws NoSuchInterfaceException {
        return (PlanningController) host.getFcInterface(PLANNING_CONTROLLER);
    }

    public static ExecutionController getExecutionController(Component host) throws NoSuchInterfaceException {
        return (ExecutionController) host.getFcInterface(EXECUTION_CONTROLLER);
    }

}
