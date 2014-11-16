package cl.niclabs.scada.acs.gcmscript.controllers.analysis;

import cl.niclabs.scada.acs.component.ACSUtils;
import cl.niclabs.scada.acs.component.controllers.analysis.AnalysisController;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.fscript.ScriptExecutionError;
import org.objectweb.fractal.fscript.diagnostics.Diagnostic;
import org.objectweb.fractal.fscript.diagnostics.Severity;
import org.objectweb.fractal.fscript.interpreter.Context;
import org.objectweb.fractal.fscript.types.Signature;
import org.objectweb.proactive.extra.component.fscript.model.AbstractGCMProcedure;
import org.objectweb.proactive.extra.component.fscript.model.GCMComponentNode;

import java.util.List;

import static org.objectweb.fractal.fscript.types.PrimitiveType.STRING;

/**
 * Created by mibanez
 */
public class PrintRulesFunction extends AbstractGCMProcedure {

    @Override
    public String getName() {
        return "print-rules";
    }

    @Override
    public Signature getSignature() {
        return new Signature(STRING, model.getNodeKind("component"));
    }

    @Override
    public boolean isPureFunction() {
        return true;
    }

    @Override
    public Object apply(List<Object> args, Context ctx) throws ScriptExecutionError {
        GCMComponentNode node = (GCMComponentNode) args.get(0);

        try {
            AnalysisController analysisController = ACSUtils.getAnalysisController(node.getComponent());
            String info = "\t(NAME)\t(SUBSCRIPTIONS)\n";

            for (String id : analysisController.getRegisteredIds()) {

                String subscriptions = "";
                for (String metricName : analysisController.getSubscriptions(id).unwrap()) {
                    subscriptions += metricName + ", ";
                }

                info += "\t" + id + "\t" + (subscriptions.equals("") ? "--" : subscriptions) + "\n";
            }

            return info += "\t --- (END) ---\n";
        } catch (NoSuchInterfaceException e) {
            throw new ScriptExecutionError(new Diagnostic(Severity.ERROR, "AnalysisController not found"), e);
        }
    }

}
