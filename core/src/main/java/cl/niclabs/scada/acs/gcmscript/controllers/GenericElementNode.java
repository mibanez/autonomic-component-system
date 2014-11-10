package cl.niclabs.scada.acs.gcmscript.controllers;

import cl.niclabs.scada.acs.component.controllers.CommunicationException;
import cl.niclabs.scada.acs.component.controllers.GenericElementProxy;
import org.objectweb.fractal.fscript.model.AbstractNode;
import org.objectweb.fractal.fscript.model.NodeKind;

import java.util.NoSuchElementException;

/**
 * Created by mibanez
 */
public abstract class GenericElementNode<ELEMENT extends GenericElementProxy> extends AbstractNode {

    private final ELEMENT elementProxy;

    public GenericElementNode(NodeKind kind, ELEMENT elementProxy) {
        super(kind);
        this.elementProxy = elementProxy;

        if (elementProxy == null) {
            throw new NullPointerException("element proxy cant be null");
        }
    }

    public ELEMENT getElementProxy() {
        return elementProxy;
    }

    @Override
    public Object getProperty(String name) {
        try {
            switch (name) {
                case "id":      return elementProxy.getId();
                case "enabled": return elementProxy.isEnabled();
                default:        throw new NoSuchElementException("Invalid property name '" + name + "'.");
            }
        } catch (CommunicationException e) {
            throw new NoSuchElementException("Internal element error: " + e.getMessage());
        }
    }

    @Override
    public void setProperty(String name, Object value) {
        checkSetRequest(name, value);
        try {
            switch (name) {
                case "enabled": elementProxy.setEnabled((boolean) value); break;
                default:        throw new NoSuchElementException("Invalid property name '" + name + "'");
            }
        } catch (CommunicationException e) {
            throw new NoSuchElementException("Internal element error: " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "[Element id=" + elementProxy.getId() + "]";
    }

}
