package cl.niclabs.scada.acs.component;

import cl.niclabs.scada.acs.component.factory.ACSTypeFactory;
import cl.niclabs.scada.acs.component.factory.ACSTypeFactoryImpl;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.Fractive;

/**
 * Created by mibanez on 08-09-14.
 */
public class ACSFractive extends Fractive {

    private ACSTypeFactory typeFactory = ACSTypeFactoryImpl.getInstance();
    private Type type = null;

    @Override
    public Object getFcInterface(String itfName) throws NoSuchInterfaceException {
        if (Constants.GENERIC_FACTORY.equals(itfName)) {
            return this;
        } else if (Constants.TYPE_FACTORY.equals(itfName)) {
            return typeFactory;
        } else {
            throw new NoSuchInterfaceException(itfName);
        }
    }

    @Override
    public Type getFcType() {
        if (type == null) {
            try {
                return type = typeFactory.createFcType(new InterfaceType[]{
                        typeFactory.createFcItfType(Constants.GENERIC_FACTORY,
                                GenericFactory.class.getName(), false, false, false),
                        typeFactory.createFcItfType(Constants.TYPE_FACTORY, TypeFactory.class.getName(),
                                false, false, false)});
            } catch (InstantiationException e) {
                // logger.error(e.getMessage());
                return null;
            }
        } else {
            return type;
        }
    }

}
