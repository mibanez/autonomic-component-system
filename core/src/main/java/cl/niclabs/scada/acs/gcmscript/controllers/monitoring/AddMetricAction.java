package cl.niclabs.scada.acs.gcmscript.controllers.monitoring;

import cl.niclabs.scada.acs.component.ACSUtils;
import cl.niclabs.scada.acs.component.controllers.DuplicatedElementIdException;
import cl.niclabs.scada.acs.component.controllers.InvalidElementException;
import cl.niclabs.scada.acs.component.controllers.MetricProxy;
import cl.niclabs.scada.acs.gcmscript.ACSModel;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.fscript.ScriptExecutionError;
import org.objectweb.fractal.fscript.diagnostics.Diagnostic;
import org.objectweb.fractal.fscript.diagnostics.Severity;
import org.objectweb.fractal.fscript.interpreter.Context;
import org.objectweb.fractal.fscript.types.Signature;
import org.objectweb.proactive.extra.component.fscript.model.AbstractGCMProcedure;

import java.util.List;

import static org.objectweb.fractal.fscript.types.PrimitiveType.STRING;

/**
 * Created by mibanez
 */
public class AddMetricAction extends AbstractGCMProcedure {

    @Override
    public String getName() {
        return "add-metric";
    }

    @Override
    public boolean isPureFunction() {
        return false;
    }

    @Override
    public Signature getSignature() {
        return new Signature(model.getNodeKind("metric"), model.getNodeKind("component"), STRING, STRING);
    }

    @Override
    public Object apply(List<Object> objects, Context context) throws ScriptExecutionError {
        Component host = (Component) objects.get(0);
        String id = (String) objects.get(1);
        String className = (String) objects.get(2);
        try {
            MetricProxy metricProxy = ACSUtils.getMonitoringController(host).add(id, className);
            return ((ACSModel) model).createMetricNode(metricProxy);
        }
        catch (NoSuchInterfaceException e) {
            throw new ScriptExecutionError(new Diagnostic(Severity.ERROR, "MonitorController not found"), e);
        } catch (InvalidElementException | DuplicatedElementIdException e) {
            throw new ScriptExecutionError(
                    new Diagnostic(Severity.WARNING, "Can't add metric, internal monitoring-controller exception"), e);
        }
    }
}
