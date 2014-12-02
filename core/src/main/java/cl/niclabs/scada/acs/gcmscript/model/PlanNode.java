package cl.niclabs.scada.acs.gcmscript.model;

import cl.niclabs.scada.acs.component.ACSManager;
import cl.niclabs.scada.acs.component.controllers.planning.PlanningController;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
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
    private final String planName;

    public PlanNode(ACSModel model, Component host, String planName) throws NoSuchInterfaceException {
        super(model.getNodeKind("plan"));
        this.host = host;
        this.planningController = ACSManager.getPlanningController(host);
        this.planName = planName;
    }

    public PlanNode(ACSModel model, Component host, PlanningController planningController, String planName) throws NoSuchInterfaceException {
        super(model.getNodeKind("plan"));
        this.host = host;
        this.planningController = planningController;
        this.planName = planName;
    }

    @Override
    public Object getProperty(String name) {
        switch (name) {
            case "name":    return planName;
            case "state":   return getState();
            default:        throw new NoSuchElementException("Invalid property name '" + name + "'.");
        }
    }

    @Override
    public void setProperty(String name, Object value) {
        checkSetRequest(name, value);
        switch (name) {
            case "state":
                setState((String) value);
                break;
            default:
                throw new NoSuchElementException("Invalid property name '" + name + "'");
        }
    }

    @Override
    public String toString() {
        return String.format("[Plan id=%s]", planName);
    }

    public Component getHost() {
        return host;
    }

    public String getPlanName() {
        return planName;
    }

    public PlanningController getPlanningController() {
        return planningController;
    }

    public String getState() {
        Wrapper<Boolean> result = planningController.isEnabled(this.getPlanName());
        if (result.isValid()) {
            return result.unwrap() ? "ENABLED" : "DISABLED";
        }
        return "UNKNOWN: " + result.getMessage();
    }

    public void setState(String state) {
        if (state.equals("ENABLED")) {
            planningController.setEnabled(this.planName, true);
        } else if (state.equals("DISABLED")) {
            planningController.setEnabled(this.planName, false);
        } else {
            throw new UnsupportedOperationException("Only ENABLED and DISABLED states supported");
        }
    }
}
