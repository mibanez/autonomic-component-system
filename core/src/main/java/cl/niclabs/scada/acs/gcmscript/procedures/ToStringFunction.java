package cl.niclabs.scada.acs.gcmscript.procedures;

import org.objectweb.fractal.fscript.ScriptExecutionError;
import org.objectweb.fractal.fscript.interpreter.Context;
import org.objectweb.fractal.fscript.types.PrimitiveType;
import org.objectweb.fractal.fscript.types.Signature;
import org.objectweb.fractal.fscript.types.Type;
import org.objectweb.proactive.extra.component.fscript.model.AbstractGCMProcedure;

import java.util.List;

public class ToStringFunction extends AbstractGCMProcedure {

    @Override
    public Signature getSignature() {
        return new Signature(PrimitiveType.STRING, new Type[] { PrimitiveType.OBJECT });
    }

    @Override
    public String getName() {
        return "to-string";
    }

    @Override
    public boolean isPureFunction() {
        return true;
    }

    @Override
    public Object apply(List<Object> list, Context context) throws ScriptExecutionError {
        return list.get(0).toString();
    }
}
