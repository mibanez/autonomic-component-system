package cl.niclabs.scada.acs.gcmscript.controllers.monitoring;

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
public class RemoveMetricAction extends AbstractGCMProcedure {

    @Override
    public String getName() {
        return "remove-metric";
    }

    @Override
    public Signature getSignature() {
        return new Signature(VoidType.VOID_TYPE, model.getNodeKind("metric"));
    }

    @Override
    public boolean isPureFunction() {
        return false;
    }

    @Override
    public Object apply(List<Object> args, Context ctx) throws ScriptExecutionError {
        MetricNode metricNode = (MetricNode) args.get(0);
        try {
            metricNode.getMonitoringController().remove(metricNode.getMetricId());
            return "Removing metric... Done.";
        } catch (ElementNotFoundException e) {
            throw new ScriptExecutionError(new Diagnostic(Severity.WARNING, "MonitoringController exception"), e);
        }
    }

}
