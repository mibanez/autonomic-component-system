package cl.niclabs.scada.acs.gcmscript.controllers.planning;

import cl.niclabs.scada.acs.component.ACSUtils;
import cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm;
import cl.niclabs.scada.acs.component.controllers.planning.PlanningController;
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
public class PrintPlansFunction extends AbstractGCMProcedure {

    @Override
    public String getName() {
        return "print-plans";
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
            PlanningController planningController = ACSUtils.getPlanningController(node.getComponent());
            String info = "\t(NAME)\t(SUBSCRIPTIONS)\n";

            for (String id : planningController.getRegisteredIds()) {

                String msg = "";
                for (PlanningController.AlarmSubscription sub: planningController.getSubscriptions(id).unwrap()) {
                    msg += String.format("%s(%s), ", sub.getRuleId(), sub.getAlarm());
                }

                ACSAlarm global = planningController.getGlobalSubscription(id).unwrap();
                if (global != null) {
                    msg += String.format("GLOBAL(%s)", global);
                }

                info += String.format("\t%s\t%s\n", id, msg.equals("") ? "--" : msg);
            }

            return info += "\t --- (END) ---\n";
        }
        catch (NoSuchInterfaceException e) {
            throw new ScriptExecutionError(new Diagnostic(Severity.ERROR, "AnalysisController not found"), e);
        }
    }

}
