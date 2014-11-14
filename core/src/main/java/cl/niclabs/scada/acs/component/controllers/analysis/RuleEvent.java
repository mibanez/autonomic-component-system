package cl.niclabs.scada.acs.component.controllers.analysis;

import java.io.Serializable;

/**
 * Created by mibanez
 */
public class RuleEvent implements Serializable {

    private final String ruleId;
    private final ACSAlarm alarm;

    RuleEvent(String ruleId, ACSAlarm alarm) {
        this.ruleId = ruleId;
        this.alarm = alarm;
    }

    public String getRuleId() {
        return ruleId;
    }

    public ACSAlarm getAlarm() {
        return alarm;
    }
}
