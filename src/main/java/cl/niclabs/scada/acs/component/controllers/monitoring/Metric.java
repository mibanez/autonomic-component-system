package cl.niclabs.scada.acs.component.controllers.monitoring;

import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;

import java.io.Serializable;

/**
 * Created by mibanez
 */
public abstract class Metric<TYPE> implements Serializable {

    public abstract Wrapper<TYPE> getValue();

}
