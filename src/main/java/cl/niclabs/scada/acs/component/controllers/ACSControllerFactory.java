package cl.niclabs.scada.acs.component.controllers;

import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;

/**
 * Created by mibanez on 07-09-14.
 */
public class ACSControllerFactory {

    private PAGCMTypeFactory typeFactory;
    private PAGenericFactory genericFactory;

    public ACSControllerFactory(PAGCMTypeFactory pagcmTypeFactory, PAGenericFactory pagcmGenericFactory) {
        this.typeFactory = pagcmTypeFactory;
        this.genericFactory = pagcmGenericFactory;
    }

    public <CONTROLLER extends AbstractACSController> void addController(Class<CONTROLLER> controllerClass, Component host) {

    }

}
