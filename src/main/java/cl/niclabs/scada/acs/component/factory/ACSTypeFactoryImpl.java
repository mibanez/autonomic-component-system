package cl.niclabs.scada.acs.component.factory;

import cl.niclabs.scada.acs.component.controllers.MonitorController;
import cl.niclabs.scada.acs.component.utils.ACSBuildHelper;
import org.apache.log4j.Logger;
import org.etsi.uri.gcm.api.type.GCMTypeFactory;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.component.type.PAComponentTypeImpl;
import org.objectweb.proactive.core.component.type.PAGCMInterfaceTypeImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mibanez on 07-09-14.
 */
public class ACSTypeFactoryImpl implements ACSTypeFactory {

    private static final Logger logger = Logger.getLogger(ACSTypeFactoryImpl.class);
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
        List<InterfaceType> nfList = new ArrayList<InterfaceType>();
        if (fInterfaceTypes == null) {
            fInterfaceTypes = new InterfaceType[]{};
        }

        nfList.addAll(ACSBuildHelper.getStandardNfInterfaces(this));
        nfList.add(createGCMItfType("monitor-controller", MonitorController.class.getName(), false, true, "singleton"));
        nfList.addAll(ACSBuildHelper.getAcsNfInterfaces(this, fInterfaceTypes));

        if (nfInterfaceTypes != null) {
            for (InterfaceType userItfType : nfInterfaceTypes) {
                for (InterfaceType acsItfType : nfList) {
                    if (userItfType.getFcItfName().equals(acsItfType.getFcItfName())) {
                        String msg = "The NF interface name \"%s\" is already in use for internal purposes.";
                        msg += "Please, rename this interface.";
                        throw new InstantiationException(String.format(msg, userItfType.getFcItfName()));
                    }
                }
            }
        }

        return new PAComponentTypeImpl(fInterfaceTypes, nfList.toArray(new InterfaceType[nfList.size()]));
    }

    @Override
    public ComponentType createFcType(InterfaceType[] fInterfaceTypes) throws InstantiationException {
        return createFcType(fInterfaceTypes, null);
    }

}
