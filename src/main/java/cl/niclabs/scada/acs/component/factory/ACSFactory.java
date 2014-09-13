package cl.niclabs.scada.acs.component.factory;

import cl.niclabs.scada.acs.component.factory.exceptions.ACSFactoryException;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.type.PAComponentTypeImpl;
import org.objectweb.proactive.core.component.type.PAGCMInterfaceType;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by mibanez
 */
public class ACSFactory {

    private static PAGCMTypeFactory tf;
    private static PAGenericFactory gf;

    private static void checkFactories() throws ACSFactoryException {
        if (tf == null || gf == null) {
            try {
                Component bootstrap = Utils.getBootstrapComponent();
                tf = Utils.getPAGCMTypeFactory(bootstrap);
                gf = Utils.getPAGenericFactory(bootstrap);
            } catch (NoSuchInterfaceException | InstantiationException e) {
                throw new ACSFactoryException("Couldn't initialize the GCM/ProActive factories", e);
            }
        }
    }

    public InterfaceType createInterfaceType(String name, String signature, boolean isClient, boolean isOptional)
            throws ACSFactoryException {
        return createInterfaceType(name, signature, isClient, isOptional, "singleton");
    }

    public InterfaceType createInterfaceType(String name, String signature, boolean isClient, boolean isOptional,
                                             String cardinality) throws ACSFactoryException {
        return createInterfaceType(name, signature, isClient, isOptional, cardinality, false);
    }

    public InterfaceType createInterfaceType(String name, String signature, boolean isClient, boolean isOptional,
                                             String cardinality, boolean isInternal) throws ACSFactoryException {
        try {
            checkFactories();
            return tf.createGCMItfType(name, signature, isClient, isOptional, cardinality, isInternal);
        } catch (InstantiationException e) {
            throw new ACSFactoryException(e);
        }
    }

    public ComponentType createComponentType(InterfaceType[] fTypes) throws ACSFactoryException {
        return createComponentType(fTypes, null);
    }

    public ComponentType createComponentType(InterfaceType[] fTypes, InterfaceType[] nfTypes)
            throws ACSFactoryException {

        checkFactories();
        List<InterfaceType> nfList = new ArrayList<>();
        if (nfTypes != null) {
            Collections.addAll(nfList, nfTypes);
        }

        List<InterfaceType> acsNfTypes = new ArrayList<>();
        acsNfTypes.addAll(BuildHelper.getStandardNfInterfaces(tf));
        acsNfTypes.addAll(BuildHelper.getAcsNfInterfaces(tf, fTypes));

        // check for conflicts between acs nf interfaces and custom defined nf interfaces
        for (InterfaceType acsNfType : acsNfTypes) {
            if (checkUnique(nfList, acsNfType)) {
                nfList.add(acsNfType);
            }
        }
        try {
            return new PAComponentTypeImpl(fTypes, nfList.toArray(new InterfaceType[nfList.size()]));
        } catch (InstantiationException e) {
            throw new ACSFactoryException("Couldn't create the component type", e);
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
    private boolean checkUnique(List<InterfaceType> nfList, InterfaceType acsNfInterface)
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
    private boolean checkEquals(InterfaceType nfInterface, InterfaceType acsNfInterface) {
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

    public PAGCMTypeFactory getTypeFactory() throws ACSFactoryException {
        checkFactories();
        return tf;
    }

    public PAGenericFactory getGenericFactory() throws ACSFactoryException {
        checkFactories();
        return gf;
    }

}
