package cl.niclabs.scada.acs.component.controllers;

import cl.niclabs.scada.acs.component.controllers.exceptions.ACSIntegrationException;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.componentcontroller.AbstractPAComponentController;
import org.objectweb.proactive.core.component.control.PAGCMLifeCycleController;
import org.objectweb.proactive.core.component.control.PAMembraneController;

/**
 * Created by mibanez on 07-09-14.
 */
public abstract class AbstractACSController extends AbstractPAComponentController {

    /**
     * Add this controller into the membrane of the host component.
     *
     * @param host
     */
    public void addInto(Component host) throws ACSIntegrationException {

        PAMembraneController membrane;
        try {
            membrane = Utils.getPAMembraneController(host);
        } catch (NoSuchInterfaceException e) {
            String msg = "Host component must implement the membrane-controller";
            throw new ACSIntegrationException(msg, e);
        }

        // To verify if the component has a STOPPED life-cycle we will a STARTED membrane
        String membraneState = membrane.getMembraneState();
        if (membraneState.equals(PAMembraneController.MEMBRANE_STOPPED)) {
            try {
                membrane.startMembrane();
            } catch (IllegalLifeCycleException e) {
                String msg = "Fail when trying to start the membrane. ";
                msg += "A reason may be a failure when trying the life-cycle of NF component in the membrane.";
                throw new ACSIntegrationException(msg, e);
            }
        }

        PAGCMLifeCycleController lifeCycle;
        try {
            lifeCycle = Utils.getPAGCMLifeCycleController(host);
        } catch (NoSuchInterfaceException e) {
            String msg = "Host component must implement the life-cycle-controller";
            throw new ACSIntegrationException(msg, e);
        }

        // The life-cycle must be STOPPED
        String lifeCycleState = lifeCycle.getFcState();
        if (lifeCycleState.equals(PAGCMLifeCycleController.STARTED)) {
            try {
                lifeCycle.stopFc();
            } catch (IllegalLifeCycleException e) {
                String msg = "Fail when trying to stop host life-cycle";
                throw new ACSIntegrationException(msg, e);
            }
        }

        // The membrane must be STOPPED also
        if (membrane.getMembraneState().equals(PAMembraneController.MEMBRANE_STARTED)) {
            try {
                membrane.stopMembrane();
            } catch (NoSuchInterfaceException | IllegalLifeCycleException e) {
                String msg = "Fail when trying to stop host membrane";
                throw new ACSIntegrationException(msg, e);
            }
        }
    }

}
