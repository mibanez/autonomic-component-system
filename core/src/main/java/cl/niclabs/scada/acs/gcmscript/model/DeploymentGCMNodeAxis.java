package cl.niclabs.scada.acs.gcmscript.model;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.fscript.model.AbstractAxis;
import org.objectweb.fractal.fscript.model.Node;
import org.objectweb.fractal.fscript.model.fractal.FractalModel;
import org.objectweb.proactive.core.body.proxy.UniversalBodyProxy;
import org.objectweb.proactive.core.component.representative.PAComponentRepresentative;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.extra.component.fscript.model.GCMComponentNode;
import org.objectweb.proactive.extra.component.fscript.model.GCMNodeFactory;

import java.util.HashSet;
import java.util.Set;


public class DeploymentGCMNodeAxis extends AbstractAxis {

    public DeploymentGCMNodeAxis(FractalModel model) {
        super(model, "deployment-gcmnode", "component", "gcmnode");
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public boolean isModifiable() {
        return false;
    }

    @Override
    public Set<Node> selectFrom(Node source) {
        HashSet<Node> gcmNode = new HashSet<>();
        GCMNodeFactory nodeFactory = (GCMNodeFactory) this.model;
        try {
            Component comp = ((GCMComponentNode) source).getComponent();
            UniversalBodyProxy ubProxy = (UniversalBodyProxy) ((PAComponentRepresentative) comp).getProxy();
            gcmNode.add(nodeFactory.createGCMNodeNode(NodeFactory.getNode(ubProxy.getBody().getNodeURL())));
        } catch (NodeException e) {
            e.printStackTrace();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        return gcmNode;
    }
}
