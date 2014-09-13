package cl.niclabs.scada.acs.component.factory;

import cl.niclabs.scada.acs.component.utils.BuildHelper;
import org.etsi.uri.gcm.api.type.GCMTypeFactory;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.component.type.PAComponentTypeImpl;
import org.objectweb.proactive.core.component.type.PAGCMInterfaceType;
import org.objectweb.proactive.core.component.type.PAGCMInterfaceTypeImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ACS TypeFactory version. This TypeFactory will add automatically the extra nf interfaces.
 *
 * Created by mibanez
 */
public class ACSTypeFactoryImpl implements ACSTypeFactory {

    private static ACSTypeFactory instance;

    private ACSTypeFactoryImpl() {
        super();
    }

    public static ACSTypeFactory getInstance() {
        if (instance == null) {
            instance = new ACSTypeFactoryImpl();
        }
        return instance;
    }

    @Override
    public InterfaceType createGCMItfType(String name, String signature, boolean isClient, boolean isOptional,
                                          String cardinality, boolean isInternal) throws InstantiationException {
        return new PAGCMInterfaceTypeImpl(name, signature, isClient, isOptional, cardinality, isInternal);
    }

    @Override
    public InterfaceType createGCMItfType(String name, String signature, boolean isClient, boolean isOptional,
                                          String cardinality) throws InstantiationException {
        return new PAGCMInterfaceTypeImpl(name, signature, isClient, isOptional, cardinality);
    }

    @Override
    public InterfaceType createFcItfType(String name, String signature, boolean isClient, boolean isOptional,
                                         boolean isCollection) throws InstantiationException {
        return new PAGCMInterfaceTypeImpl(name, signature, isClient, isOptional,
                (isCollection ? GCMTypeFactory.COLLECTION_CARDINALITY : GCMTypeFactory.SINGLETON_CARDINALITY));
    }

    @Override
    public ComponentType createFcType(InterfaceType[] fInterfaceTypes, InterfaceType[] nfInterfaceTypes) throws InstantiationException {

        List<InterfaceType> nfList = new ArrayList<>();
        if (nfInterfaceTypes != null) {
            Collections.addAll(nfList, nfInterfaceTypes);
        }

        if (fInterfaceTypes == null) {
            fInterfaceTypes = new InterfaceType[]{};
        }

        List<InterfaceType> acsNfInterfaces = new ArrayList<>();
        acsNfInterfaces.addAll(BuildHelper.getStandardNfInterfaces(this));
        acsNfInterfaces.addAll(BuildHelper.getAcsNfInterfaces(this, fInterfaceTypes));

        // check for conflicts between acs nf interfaces and custom defined nf interfaces
        for (InterfaceType acsNfInterface : acsNfInterfaces) {
            if (checkUnique(nfList, acsNfInterface)) {
                nfList.add(acsNfInterface);
            }
        }

        return new PAComponentTypeImpl(fInterfaceTypes, nfList.toArray(new InterfaceType[nfList.size()]));
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
            throws InstantiationException {

        for (InterfaceType nfInterface : nfList) {
            if (nfInterface.getFcItfName().equals(acsNfInterface.getFcItfName())) {
                if (checkEquals(nfInterface, acsNfInterface)) {
                    return false;
                }
                String msg = "The NF interface name \"%s\" is already in use for internal purposes.";
                msg += "Please, rename this interface.";
                throw new InstantiationException(String.format(msg, acsNfInterface.getFcItfName()));
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

    @Override
    public ComponentType createFcType(InterfaceType[] fInterfaceTypes) throws InstantiationException {
        return createFcType(fInterfaceTypes, null);
    }

    @Override
    public ComponentType createNfFcType(InterfaceType[] interfaceTypes) throws InstantiationException {
        if (interfaceTypes == null) {
            interfaceTypes = new InterfaceType[]{};
        }
        return new PAComponentTypeImpl(interfaceTypes, new InterfaceType[]{});
    }

}
