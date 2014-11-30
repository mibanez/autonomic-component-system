package cl.niclabs.scada.acs.component.controllers.monitoring.records;

import java.io.Serializable;

public interface RCondition<RECORD extends Record> extends Serializable {

    boolean evaluate(RECORD record);
}
