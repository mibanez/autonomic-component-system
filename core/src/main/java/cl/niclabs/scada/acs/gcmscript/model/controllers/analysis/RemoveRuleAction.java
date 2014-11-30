package cl.niclabs.scada.acs.gcmscript.model.controllers.analysis;

import cl.niclabs.scada.acs.component.controllers.ElementNotFoundException;
import org.objectweb.fractal.fscript.ScriptExecutionError;
import org.objectweb.fractal.fscript.diagnostics.Diagnostic;
import org.objectweb.fractal.fscript.diagnostics.Severity;
import org.objectweb.fractal.fscript.interpreter.Context;
import org.objectweb.fractal.fscript.types.Signature;
import org.objectweb.fractal.fscript.types.VoidType;
import org.objectweb.proactive.extra.component.fscript.model.AbstractGCMProcedure;

import java.util.List;

/**
 * Created by mibanez
 */
public class RemoveRuleAction extends AbstractGCMProcedure {

    @Override
    public String getName() {
        return "remove-rule";
    }

    @Override
    public Signature getSignature() {
        return new Signature(VoidType.VOID_TYPE, model.getNodeKind("rule"));
    }

    @Override
    public boolean isPureFunction() {
        return false;
    }

    @Override
    public Object apply(List<Object> args, Context ctx) throws ScriptExecutionError {
        RuleNode ruleNode = (RuleNode) args.get(0);
        try {
            ruleNode.getAnalysisController().remove(ruleNode.getRuleId());
            return "Removing rule... Done.";
        } catch (ElementNotFoundException e) {
            throw new ScriptExecutionError(new Diagnostic(Severity.WARNING, "AnalysisController exception"), e);
        }
    }
}
