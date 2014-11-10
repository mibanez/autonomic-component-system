package cl.niclabs.scada.acs.component.adl;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.proactive.core.component.adl.FactoryFactory;

import java.util.HashMap;


public class ACSAdlFactoryFactory {

    private final static String ACS_FACTORY = "cl.niclabs.scada.acs.component.adl.ACSAdlFactory";

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Factory getACSAdlFactory() throws ADLException {
        return org.objectweb.fractal.adl.FactoryFactory.getFactory(ACS_FACTORY,
        		FactoryFactory.PROACTIVE_BACKEND, new HashMap());
    }
    
}
