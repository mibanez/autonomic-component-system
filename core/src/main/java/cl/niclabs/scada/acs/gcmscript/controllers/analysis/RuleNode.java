package cl.niclabs.scada.acs.gcmscript.controllers.analysis;

import cl.niclabs.scada.acs.component.controllers.CommunicationException;
import cl.niclabs.scada.acs.component.controllers.RuleProxy;
import cl.niclabs.scada.acs.gcmscript.ACSModel;
import cl.niclabs.scada.acs.gcmscript.controllers.GenericElementNode;

import java.util.NoSuchElementException;

/**
 * Created by mibanez
 */
public class RuleNode extends GenericElementNode<RuleProxy> {

    public RuleNode(ACSModel model, RuleProxy ruleProxy) {
        super(model.getNodeKind("rule"), ruleProxy);
    }

    @Override
    public Object getProperty(String name) {
        try {
            return super.getProperty(name);
        } catch (NoSuchElementException parentException) {
            if (name.equals("alarm")) {
                try {
                    return getElementProxy().getAlarm();
                } catch (CommunicationException e) {
                    throw new NoSuchElementException("Internal element error: " + e.getMessage());
                }
            } else {
                throw parentException;
            }
        }
    }

    @Override
    public String toString() {
        return "[Rule id=" + getElementProxy().getId() + "]";
    }

}
