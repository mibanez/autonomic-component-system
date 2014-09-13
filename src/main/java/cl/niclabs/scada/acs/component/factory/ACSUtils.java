package cl.niclabs.scada.acs.component.factory;

import cl.niclabs.scada.acs.component.factory.exceptions.ACSFactoryException;
import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.component.control.PAGCMLifeCycleController;
import org.objectweb.proactive.core.component.control.PAMembraneController;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for BuildHelper
 *
 * Created by mibanez
 */
class ACSUtils {

    private static final Logger logger = LoggerFactory.getLogger(BuildHelper.class);

    protected static String getComponentName(Component component) {
        if (component instanceof PAComponent) {
            return ((PAComponent) component).getComponentParameters().getName();
        }
        try {
            return GCM.getNameController(component).getFcName();
        } catch (NoSuchInterfaceException e) {
            return "*unknown*";
        }
    }

    protected static void startLifeCycle(PAGCMLifeCycleController lifeCycle, String where, String who)
            throws ACSFactoryException {
        try {
            logger.debug("{}: starting the life-cycle on component {}", where, who);
            lifeCycle.startFc();
        } catch (IllegalLifeCycleException e) {
            String msg = where + ": LifeCycle cannot be started on component " + who;
            logger.error(msg);
            throw new ACSFactoryException(msg, e);
        }
    }

    protected static void startMembrane(PAMembraneController membrane, String where, String who)
            throws ACSFactoryException {
        try {
            logger.debug("{}: starting the membrane on component {}", where, who);
            membrane.startMembrane();
        } catch (IllegalLifeCycleException e) {
            String msg = where + ": Membrane cannot be started on component " + who;
            logger.error(msg);
            throw new ACSFactoryException(msg, e);
        }
    }

    protected static void stopLifeCycle(PAGCMLifeCycleController lifeCycle, String where, String who)
            throws ACSFactoryException {
        try {
            logger.debug("{}: stopping the life-cycle on component {}", where, who);
            lifeCycle.stopFc();
        } catch (IllegalLifeCycleException e) {
            String msg = where + ": LifeCycle cannot be stopped on component " + who;
            logger.error(msg);
            throw new ACSFactoryException(msg, e);
        }
    }

    protected static void stopMembrane(PAMembraneController membrane, String where, String who)
            throws ACSFactoryException {
        try {
            logger.debug("{}: stopping the membrane on component {}", where, who);
            membrane.stopMembrane();
        } catch (NoSuchInterfaceException | IllegalLifeCycleException e) {
            String msg = where + ": Membrane cannot be stopped on component " + who;
            logger.error(msg);
            throw new ACSFactoryException(msg, e);
        }
    }

}
