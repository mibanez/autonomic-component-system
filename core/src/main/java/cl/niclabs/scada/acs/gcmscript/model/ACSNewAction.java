package cl.niclabs.scada.acs.gcmscript.model;

import org.objectweb.fractal.fscript.ScriptExecutionError;
import org.objectweb.fractal.fscript.interpreter.Context;
import org.objectweb.fractal.fscript.types.*;
import org.objectweb.proactive.extra.component.fscript.model.GCMNewAction;
import org.objectweb.proactive.extra.component.fscript.model.GCMNodeNode;

import java.util.HashSet;
import java.util.List;

public class ACSNewAction extends GCMNewAction {

    @Override
    public Signature getSignature() {
        return new Signature(this.model.getNodeKind("component"), new Type[] {
                PrimitiveType.STRING,
                new UnionType(new Type[] {
                        this.model.getNodeKind("gcmapplication"),
                        new NodeSetType(this.model.getNodeKind("gcmvirtualnode")),
                        new NodeSetType(this.model.getNodeKind("gcmnode")),
                        this.model.getNodeKind("gcmnode")})});
    }

    @Override
    public Object apply(List<Object> args, Context ctx) throws ScriptExecutionError {

        if (args.get(1) instanceof GCMNodeNode) {
            HashSet<GCMNodeNode> set = new HashSet<>();
            set.add((GCMNodeNode) args.get(1));
            args.set(1, set);
        }

        return super.apply(args, ctx);
    }
}
