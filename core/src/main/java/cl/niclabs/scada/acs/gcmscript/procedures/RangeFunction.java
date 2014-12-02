package cl.niclabs.scada.acs.gcmscript.procedures;

import org.objectweb.fractal.fscript.ScriptExecutionError;
import org.objectweb.fractal.fscript.interpreter.Context;
import org.objectweb.fractal.fscript.types.NodeSetType;
import org.objectweb.fractal.fscript.types.PrimitiveType;
import org.objectweb.fractal.fscript.types.Signature;
import org.objectweb.fractal.fscript.types.Type;
import org.objectweb.proactive.extra.component.fscript.model.AbstractGCMProcedure;

import java.util.HashSet;
import java.util.List;

public class RangeFunction extends AbstractGCMProcedure {

    @Override
    public Object apply(List<Object> list, Context context) throws ScriptExecutionError {
        HashSet<Integer> result = new HashSet<>();
        int n1 = (int)list.get(0);
        int n2 = (int)list.get(1);
        if (n1 < n2) {
            for (int i = n1; n1 < n2; i++) {
                result.add(i);
            }
        } else {
            for (int i = n1; n1 > n2; i--) {
                result.add(i);
            }
        }

        /**
         * FAIL..
         */
        return result;
    }

    @Override
    public String getName() {
        return "range";
    }

    @Override
    public boolean isPureFunction() {
        return true;
    }

    @Override
    public Signature getSignature() {
        return new Signature(NodeSetType.ANY_NODE_SET_TYPE, new Type[] { PrimitiveType.NUMBER, PrimitiveType.NUMBER });
    }
}
