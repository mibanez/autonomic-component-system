package cl.niclabs.scada.acs.gcmscript.model;

import cl.niclabs.scada.acs.component.ACSManager;
import cl.niclabs.scada.acs.component.controllers.execution.ExecutionController;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.fscript.ScriptExecutionError;
import org.objectweb.fractal.fscript.ast.SourceLocation;
import org.objectweb.fractal.fscript.diagnostics.Diagnostic;
import org.objectweb.fractal.fscript.interpreter.Context;
import org.objectweb.fractal.fscript.types.PrimitiveType;
import org.objectweb.fractal.fscript.types.Signature;
import org.objectweb.fractal.fscript.types.Type;
import org.objectweb.proactive.extra.component.fscript.model.GCMComponentNode;
import org.objectweb.proactive.extra.component.fscript.model.RemoteExecuteAction;

import java.util.List;


public class ACSRemoteExecuteAction extends RemoteExecuteAction {

    public Signature getSignature() {
        return new Signature(PrimitiveType.BOOLEAN, new Type[]{this.model.getNodeKind("component"), PrimitiveType.STRING});
    }

    public Object apply(List<Object> args, Context ctx) throws ScriptExecutionError {
        if(args.get(0) instanceof GCMComponentNode) {
            try {
                Component re = ((GCMComponentNode)args.get(0)).getComponent();
                ExecutionController rc = ACSManager.getExecutionController(re);
                return rc.execute((String)args.get(1)).isValid();
            } catch (NoSuchInterfaceException var5) {
                throw new ScriptExecutionError(Diagnostic.error(SourceLocation.UNKNOWN, "Unable to execute code fragment remotely"), var5);
            }
        }
        return false;
    }
}
