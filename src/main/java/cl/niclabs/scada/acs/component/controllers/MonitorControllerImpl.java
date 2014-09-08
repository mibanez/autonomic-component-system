package cl.niclabs.scada.acs.component.controllers;

/**
 * Created by mibanez on 07-09-14.
 */
public class MonitorControllerImpl extends AbstractACSController implements MonitorController {

    public MonitorControllerImpl() {

    }
    /*
    @Override
    protected ComponentType getComponentType(PAGCMTypeFactory typeFactory) throws InstantiationException {
        return typeFactory.createFcType(new InterfaceType[] {
                typeFactory.createGCMItfType("monitor-controller", MonitorController.class.getName(),
                        PAGCMTypeFactory.CLIENT, PAGCMTypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY),
        });
    }
    */
}
