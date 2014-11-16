package cl.niclabs.scada.acs.gcmscript.controllers.planning;

import cl.niclabs.scada.acs.component.ACSUtils;
import cl.niclabs.scada.acs.component.controllers.planning.PlanningController;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.gcmscript.ACSModel;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.fscript.model.AbstractNode;

import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * Created by mibanez
 */
public class PlanNode extends AbstractNode {

    private final Component host;
    private final PlanningController planningController;
    private final String planId;

    public PlanNode(ACSModel model, Component host, String planId) throws NoSuchInterfaceException {
        super(model.getNodeKind("plan"));
        this.host = host;
        this.planningController = ACSUtils.getPlanningController(host);
        this.planId = planId;
    }

    public PlanNode(ACSModel model, Component host, PlanningController planningController, String planId) throws NoSuchInterfaceException {
        super(model.getNodeKind("plan"));
        this.host = host;
        this.planningController = planningController;
        this.planId = planId;
    }

    @Override
    public Object getProperty(String name) {
        switch (name) {
            case "id":      return planId;
            case "enabled": return isEnabled();
            default:        throw new NoSuchElementException("Invalid property name '" + name + "'.");
        }
    }

    @Override
    public void setProperty(String name, Object value) {
        checkSetRequest(name, value);
        switch (name) {
            case "enabled": setEnabled((boolean) value); break;
            default:        throw new NoSuchElementException("Invalid property name '" + name + "'");
        }
    }

    @Override
    public String toString() {
        return String.format("[Plan id=%s]", planId);
    }

    private Object isEnabled() {
        return checkWrapper(planningController.isEnabled(planId));
    }

    private void setEnabled(boolean enabled) {
        checkWrapper(planningController.setEnabled(planId, enabled));
    }

    private Object checkWrapper(Wrapper<? extends Serializable> wrapper) {
        if (wrapper.isValid()) {
            return wrapper.unwrap();
        }
        throw new NoSuchElementException(wrapper.getMessage());
    }

    public Component getHost() {
        return host;
    }

    public String getPlanId() {
        return planId;
    }

    public PlanningController getPlanningController() {
        return planningController;
    }
}
