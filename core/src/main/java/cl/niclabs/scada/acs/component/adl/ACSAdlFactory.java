package cl.niclabs.scada.acs.component.adl;

import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.component.adl.PAFactory;

import java.util.Map;

public interface ACSAdlFactory extends PAFactory {

	@SuppressWarnings("rawtypes")
	public Component newACSComponent(String name, Map context) throws Exception;
	
}
