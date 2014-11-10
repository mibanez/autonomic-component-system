package cl.niclabs.scada.acs.gcmscript.controllers.monitoring;

import cl.niclabs.scada.acs.component.controllers.CommunicationException;
import cl.niclabs.scada.acs.component.controllers.MetricProxy;
import cl.niclabs.scada.acs.gcmscript.ACSModel;
import cl.niclabs.scada.acs.gcmscript.controllers.GenericElementNode;

import java.util.NoSuchElementException;

/**
 * Created by mibanez
 */
public class MetricNode extends GenericElementNode<MetricProxy> {

    public MetricNode(ACSModel model, MetricProxy metricProxy) {
        super(model.getNodeKind("metric"), metricProxy);
    }

    @Override
    public Object getProperty(String name) {
        try {
            return super.getProperty(name);
        } catch (NoSuchElementException parentException) {
            if (name.equals("value")) {
                try {
                    return getElementProxy().getValue();
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
        return "[Metric id=" + getElementProxy().getId() + "]";
    }

}
