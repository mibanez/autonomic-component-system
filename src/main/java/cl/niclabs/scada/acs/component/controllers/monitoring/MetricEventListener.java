package cl.niclabs.scada.acs.component.controllers.monitoring;

/**
 * Created by mibanez
 */
public interface MetricEventListener {

    void notifyUpdate(MetricEvent event);

}
