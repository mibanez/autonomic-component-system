package cl.niclabs.scada.acs.component.controllers;

import org.objectweb.fractal.api.Component;

import java.io.Serializable;

/**
 * Created by mibanez
 */
public abstract class GenericElementProxy implements Serializable {

    private final String id;
    private final Component host;

    GenericElementProxy(String id, Component host) {
        this.id = id;
        this.host = host;
    }

    public abstract boolean isEnabled() throws CommunicationException;
    public abstract void setEnabled(boolean enabled) throws CommunicationException;

    public String getId() {
        return id;
    }

    public Component getHost() {
        return host;
    }

}
