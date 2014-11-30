package cl.niclabs.scada.acs.component.adl;

import cl.niclabs.scada.acs.component.factory.ACSFactory;
import cl.niclabs.scada.acs.component.factory.BuildHelper;
import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.adl.PABasicFactory;
import org.objectweb.proactive.core.component.identity.PAComponent;

import java.util.Map;

public class ACSAdlFactoryImpl extends PABasicFactory implements ACSAdlFactory {

	@Override
	@SuppressWarnings("rawtypes")
	public Object newACSComponent(String name, Map context) throws Exception {
        Component component = (Component) super.newComponent(name, context);
        addACSControllers(component, new BuildHelper(new ACSFactory()));
		return component;
	}

	private void addACSControllers(Component component, BuildHelper buildHelper) throws Exception {
        buildHelper.addACSControllers(component);
		if (((PAComponent) component).getComponentParameters().getHierarchicalType().equals(Constants.COMPOSITE)) {
			for (Component subComponent : Utils.getPAContentController(component).getFcSubComponents()) {
                addACSControllers(subComponent, buildHelper);
			}
		}
	}

}

