package cl.niclabs.scada.acs.gcmscript.controllers.analysis;

import cl.niclabs.scada.acs.component.ACSUtils;
import cl.niclabs.scada.acs.component.controllers.analysis.AnalysisController;
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
public class RuleNode extends AbstractNode {

    private final Component host;
    private final AnalysisController analysisController;
    private final String ruleId;

    public RuleNode(ACSModel model, Component host, String ruleId) throws NoSuchInterfaceException {
        super(model.getNodeKind("rule"));
        this.host = host;
        this.analysisController = ACSUtils.getAnalysisController(host);
        this.ruleId = ruleId;
    }

    public RuleNode(ACSModel model, Component host, AnalysisController analysisController, String ruleId) throws NoSuchInterfaceException {
        super(model.getNodeKind("rule"));
        this.host = host;
        this.analysisController = analysisController;
        this.ruleId = ruleId;
    }

    @Override
    public Object getProperty(String name) {
        switch (name) {
            case "id":      return ruleId;
            case "enabled": return isEnabled();
            case "alarm":   return getAlarm();
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
        return String.format("[Rule id=%s]", ruleId);
    }

    private Object isEnabled() {
        return checkWrapper(analysisController.isEnabled(ruleId));
    }

    private void setEnabled(boolean enabled) {
        checkWrapper(analysisController.setEnabled(ruleId, enabled));
    }

    private Object getAlarm() {
        return checkWrapper(analysisController.getAlarm(ruleId));
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

    public String getRuleId() {
        return ruleId;
    }
}
