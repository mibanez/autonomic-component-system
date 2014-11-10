package cl.niclabs.scada.acs.gcmscript.controllers.planning;

import cl.niclabs.scada.acs.component.controllers.PlanProxy;
import cl.niclabs.scada.acs.gcmscript.ACSModel;
import cl.niclabs.scada.acs.gcmscript.controllers.GenericElementNode;

/**
 * Created by mibanez
 */
public class PlanNode extends GenericElementNode<PlanProxy> {

    public PlanNode(ACSModel model, PlanProxy planProxy) {
        super(model.getNodeKind("plan"), planProxy);
    }

    @Override
    public String toString() {
        return "[Plan id=" + getElementProxy().getId() + "]";
    }

}
