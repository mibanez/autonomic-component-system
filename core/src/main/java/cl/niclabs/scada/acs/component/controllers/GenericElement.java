package cl.niclabs.scada.acs.component.controllers;

import java.io.Serializable;

/**
 * Created by mibanez
 */
public abstract class GenericElement implements Serializable {

    private boolean enabled = false;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

}
