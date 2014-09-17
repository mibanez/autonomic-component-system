package cl.niclabs.scada.acs.component.factory;

import cl.niclabs.scada.acs.component.factory.exceptions.ACSFactoryException;
import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.core.component.type.PAGCMInterfaceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Utility methods for BuildHelper
 *
 * Created by mibanez
 */
class ACSUtils {

    private static final Logger logger = LoggerFactory.getLogger(BuildHelper.class);

    static String getComponentName(Component component) {
        if (component instanceof PAComponent) {
            return ((PAComponent) component).getComponentParameters().getName();
        }
        try {
            return GCM.getNameController(component).getFcName();
        } catch (NoSuchInterfaceException e) {
            return "*unknown*";
        }
    }

    static void startLifeCycle(Component component, String where)
            throws ACSFactoryException {
        try {
            logger.debug("{}: starting the life-cycle on component {}", where, getComponentName(component));
            Utils.getPAGCMLifeCycleController(component).startFc();
        } catch (NoSuchInterfaceException | IllegalLifeCycleException e) {
            String msg = where + ": LifeCycle cannot be started on component " + getComponentName(component);
            logger.error(msg);
            throw new ACSFactoryException(msg, e);
        }
    }

    static void startMembrane(Component component, String where)
            throws ACSFactoryException {
        try {
            logger.debug("{}: starting the membrane on component {}", where, getComponentName(component));
            Utils.getPAMembraneController(component).startMembrane();
        } catch (NoSuchInterfaceException | IllegalLifeCycleException e) {
            String msg = where + ": Membrane cannot be started on component " + getComponentName(component);
            logger.error(msg);
            throw new ACSFactoryException(msg, e);
        }
    }

    static void stopLifeCycle(Component component, String where)
            throws ACSFactoryException {
        try {
            logger.debug("{}: stopping the life-cycle on component {}", where, getComponentName(component));
            Utils.getPAGCMLifeCycleController(component).stopFc();
        } catch (NoSuchInterfaceException | IllegalLifeCycleException e) {
            String msg = where + ": LifeCycle cannot be stopped on component " + getComponentName(component);
            logger.error(msg);
            throw new ACSFactoryException(msg, e);
        }
    }

    static void stopMembrane(Component component, String where)
            throws ACSFactoryException {
        try {
            logger.debug("{}: stopping the membrane on component {}", where, getComponentName(component));
            Utils.getPAMembraneController(component).stopMembrane();
        } catch (NoSuchInterfaceException | IllegalLifeCycleException e) {
            String msg = where + ": Membrane cannot be stopped on component " + getComponentName(component);
            logger.error(msg);
            throw new ACSFactoryException(msg, e);
        }
    }

    /**
     * Checks if the "acsNfInterface" is unique among the nf interfaces of the "nfList" list.<br>
     * <li>Returns true if it is unique.</li>
     * <li>If the name of "acsNfInterface" exists and they are exactly the same interfaces, returns false.</li>
     * <li>If the name exists and they are different interfaces, an exception is thrown.</li>
     *
     * @param nfList            list of nf interfaces
     * @param acsNfInterface    nf interfaces to check its uniqueness
     */
    static boolean checkUnique(List<InterfaceType> nfList, InterfaceType acsNfInterface)
            throws ACSFactoryException {

        for (InterfaceType nfInterface : nfList) {
            if (nfInterface.getFcItfName().equals(acsNfInterface.getFcItfName())) {
                if (checkEquals(nfInterface, acsNfInterface)) {
                    return false;
                }
                String msg = "The NF interface name \"%s\" is already in use for internal purposes.";
                msg += "Please, rename this interface.";
                throw new ACSFactoryException(String.format(msg, acsNfInterface.getFcItfName()));
            }
        }
        return true;
    }

    /**
     * Check if interfaces are exactly the same.
     * @param nfInterface       a interface to check
     * @param acsNfInterface    the other interface to check
     * @return True if they are the same, false otherwise.
     */
    private static boolean checkEquals(InterfaceType nfInterface, InterfaceType acsNfInterface) {
        if ((nfInterface.getFcItfName().equals(acsNfInterface.getFcItfName())) &&
                (nfInterface.isFcClientItf() == acsNfInterface.isFcClientItf()) &&
                (nfInterface.isFcOptionalItf() == acsNfInterface.isFcOptionalItf()) &&
                (nfInterface.getFcItfSignature().equals(acsNfInterface.getFcItfSignature()))) {
            if ((nfInterface instanceof PAGCMInterfaceType) && (acsNfInterface instanceof PAGCMInterfaceType)) {
                PAGCMInterfaceType itf1 = (PAGCMInterfaceType) nfInterface;
                PAGCMInterfaceType itf2 = (PAGCMInterfaceType) acsNfInterface;
                if ((itf1.isGCMSingletonItf() == itf2.isGCMSingletonItf()) &&
                        (itf1.isGCMMulticastItf() == itf2.isGCMMulticastItf()) &&
                        (itf1.isGCMGathercastItf() == itf2.isGCMGathercastItf()) &&
                        (itf1.isInternal() == itf2.isInternal())) {
                    return true;
                }
            } else {
                if (nfInterface.isFcCollectionItf() == acsNfInterface.isFcCollectionItf()) {
                    return true;
                }
            }
        }
        return false;
    }

}
