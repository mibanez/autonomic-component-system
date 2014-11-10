package cl.niclabs.scada.acs.gcmscript.controllers;

import org.objectweb.fractal.fscript.model.Axis;
import org.objectweb.fractal.fscript.model.Connector;
import org.objectweb.fractal.fscript.types.PrimitiveType;
import org.objectweb.fractal.fscript.types.Signature;
import org.objectweb.fractal.fscript.types.VoidType;

/**
 * Created by mibanez
 */
public class ACSConnector extends Connector {

    private final Axis axis;

    public ACSConnector(Axis axis) {
        super(axis);
        this.axis = axis;
    }

    @Override
    public Signature getSignature() {
        return new Signature(VoidType.VOID_TYPE, axis.getInputNodeType(), axis.getOutputNodeType(), PrimitiveType.STRING);
    }
}
