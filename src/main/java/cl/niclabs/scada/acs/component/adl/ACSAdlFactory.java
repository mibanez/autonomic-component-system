package cl.niclabs.scada.acs.component.adl;

import org.objectweb.proactive.core.component.adl.PAFactory;

import java.util.Map;

public interface ACSAdlFactory extends PAFactory {

	@SuppressWarnings("rawtypes")
	public Object newACSComponent(String name, Map context) throws Exception;
	
}
