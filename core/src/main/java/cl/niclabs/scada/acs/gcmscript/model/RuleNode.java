package cl.niclabs.scada.acs.gcmscript.model;

import cl.niclabs.scada.acs.component.ACSManager;
import cl.niclabs.scada.acs.component.controllers.analysis.AnalysisController;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.fscript.model.AbstractNode;

import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * Created by mibanez
 */
public class RuleNode extends AbstractNode {

    private final Component host;
    private final AnalysisController analysisController;
    private final String ruleName;

    public RuleNode(ACSModel model, Component host, String ruleName) throws NoSuchInterfaceException {
        super(model.getNodeKind("rule"));
        this.host = host;
        this.analysisController = ACSManager.getAnalysisController(host);
        this.ruleName = ruleName;
    }

    public RuleNode(ACSModel model, Component host, AnalysisController analysisController, String ruleName) throws NoSuchInterfaceException {
        super(model.getNodeKind("rule"));
        this.host = host;
        this.analysisController = analysisController;
        this.ruleName = ruleName;
    }

    @Override
    public Object getProperty(String name) {
        switch (name) {
            case "name":    return ruleName;
            case "state":   return getState();
            case "alarm":   return getAlarm();
            default:        throw new NoSuchElementException("Invalid property name '" + name + "'.");
        }
    }

    @Override
    public void setProperty(String name, Object value) {
        checkSetRequest(name, value);
        switch (name) {
            case "state":   setState((String) value); break;
            default:        throw new NoSuchElementException("Invalid property name '" + name + "'");
        }
    }

    @Override
    public String toString() {
        return String.format("[Rule id=%s]", ruleName);
    }

    private Object getAlarm() {
        return checkWrapper(analysisController.getAlarm(ruleName));
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

    public AnalysisController getAnalysisController() {
        return analysisController;
    }

    public String getRuleName() {
        return ruleName;
    }

    public String getState() {
        Wrapper<Boolean> result = analysisController.isEnabled(ruleName);
        if (result.isValid()) {
            return result.unwrap() ? "ENABLED" : "DISABLED";
        }
        return "UNKNOWN: " + result.getMessage();
    }

    public void setState(String state) {
        if (state.equals("ENABLED")) {
            analysisController.setEnabled(ruleName, true);
        } else if (state.equals("DISABLED")) {
            analysisController.setEnabled(ruleName, false);
        } else {
            throw new UnsupportedOperationException("Only ENABLED and DISABLED states supported");
        }
    }
}
