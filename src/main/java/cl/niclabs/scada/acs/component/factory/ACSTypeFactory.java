package cl.niclabs.scada.acs.component.factory;

import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;

/**
 * Created by mibanez on 07-09-14.
 */
public interface ACSTypeFactory extends PAGCMTypeFactory {

    public ComponentType createNfFcType(InterfaceType[] interfaceTypes) throws org.objectweb.fractal.api.factory.InstantiationException;

}
