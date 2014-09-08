package cl.niclabs.scada.acs.component.utils;

import cl.niclabs.scada.acs.component.controllers.MonitorController;
import cl.niclabs.scada.acs.component.controllers.MonitorControllerMulticast;
import org.apache.log4j.Logger;
import org.objectweb.fractal.api.control.NameController;
import org.objectweb.fractal.api.factory.*;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.control.*;
import org.objectweb.proactive.core.component.type.PAGCMInterfaceType;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mibanez on 08-09-14.
 */
public class ACSBuildHelper {

    public static final Logger logger = Logger.getLogger(ACSBuildHelper.class);

    // Normally, the NF interfaces mentioned here should be those that are going to be implemented by NF component,
    // and the rest of the NF interfaces (that are going to be implemented by object controller) should be in a
    // ControllerDesc file. But the PAComponentImpl ignores the NFType if there is a ControllerDesc file specified,
    // so I better put all the NF interfaces here.
    // That means that I need another method to add the object controllers for the (not yet created) controllers.
    // -- cruz
    public static List<InterfaceType> getStandardNfInterfaces(PAGCMTypeFactory tf) throws InstantiationException {
        ArrayList<InterfaceType> nfInterfaces = new ArrayList<InterfaceType>();
        logger.info("generating standard nf interfaces...");
        nfInterfaces.add(tf.createGCMItfType(Constants.CONTENT_CONTROLLER, PAContentController.class.getName(), false, false, "singleton"));
        nfInterfaces.add(tf.createGCMItfType(Constants.BINDING_CONTROLLER, PABindingController.class.getName(), false, false, "singleton"));
        nfInterfaces.add(tf.createGCMItfType(Constants.LIFECYCLE_CONTROLLER, PAGCMLifeCycleController.class.getName(), false, false, "singleton"));
        nfInterfaces.add(tf.createGCMItfType(Constants.SUPER_CONTROLLER, PASuperController.class.getName(), false, false, "singleton"));
        nfInterfaces.add(tf.createGCMItfType(Constants.NAME_CONTROLLER, NameController.class.getName(), false, false, "singleton"));
        nfInterfaces.add(tf.createGCMItfType(Constants.MEMBRANE_CONTROLLER, PAMembraneController.class.getName(), false, false, "singleton"));
        nfInterfaces.add(tf.createGCMItfType(Constants.MULTICAST_CONTROLLER, PAMulticastController.class.getName(), false, true, "singleton"));
        return nfInterfaces;
    }

    /**
     * TODO: write documentation
     *
     * @param fnInterfaceTypes
     * @return
     * @throws org.objectweb.fractal.api.factory.InstantiationException
     */
    public static List<InterfaceType> getAcsNfInterfaces(PAGCMTypeFactory tf, InterfaceType[] fnInterfaceTypes)
            throws InstantiationException {

        logger.info("generating acs nf interfaces...");
        ArrayList<InterfaceType> acsInterfaces = new ArrayList<InterfaceType>();

        for (InterfaceType itf : fnInterfaceTypes) {

            logger.info("-- checking itf: " + itf.getFcItfName());
            boolean isSingleton = !itf.isFcCollectionItf();
            if (itf instanceof PAGCMInterfaceType) {
                isSingleton = isSingleton && ((PAGCMInterfaceType) itf).isGCMSingletonItf();
            }

            if (!itf.isFcClientItf() && isSingleton) {
                acsInterfaces.add(tf.createGCMItfType(itf.getFcItfName() + "-internal-monitor-controller",
                        MonitorController.class.getName(), true, true, "singleton", true));
                logger.info("---- " + itf.getFcItfName() + "-internal-monitor-controller generated");
            }

            if (itf.isFcClientItf()) {
                if (isSingleton) {
                    acsInterfaces.add(tf.createGCMItfType(itf.getFcItfName() + "-external-monitor-controller",
                            MonitorController.class.getName(), true, true, "singleton"));

                    logger.info("---- " + itf.getFcItfName() + "-external-monitor-controller generated [from singleton]");
                }
                else if (itf instanceof PAGCMInterfaceType) {
                    if (((PAGCMInterfaceType) itf).isGCMGathercastItf()) {
                        acsInterfaces.add(tf.createGCMItfType(itf.getFcItfName() + "-external-monitor-controller",
                                MonitorController.class.getName(), true, true, "singleton"));
                        logger.info("---- " + itf.getFcItfName() + "-external-monitor-controller generated [from gathercast]");
                    }
                    else if (((PAGCMInterfaceType) itf).isGCMMulticastItf()) {
                        acsInterfaces.add(tf.createGCMItfType(itf.getFcItfName() + "-external-monitor-controller",
                                MonitorControllerMulticast.class.getName(), true, true, "multicast"));
                        logger.info("---- " + itf.getFcItfName() + "-external-monitor-controller generated [from multicast]");
                    }
                }
            }
        }

        acsInterfaces.add(tf.createGCMItfType("internal-server-monitor-controller",
                MonitorController.class.getName(), false, true, "singleton", true));
        logger.info("-- internal-monitor-controller generated");

        return acsInterfaces;
    }

}
