package cl.niclabs.scada.acs.component.controllers.monitoring;

import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;

import java.io.Serializable;

/**
 * Created by mibanez
 */
public abstract class Metric<TYPE extends Serializable> implements Serializable {

    public abstract TYPE getValue();

    public Wrapper<TYPE> getWrappedValue() {
        return new Wrapper<>(getValue());
    }

}
