package cl.niclabs.scada.acs.component.controllers.analysis;

import java.io.Serializable;

/**
 * Created by mibanez
 */
public class RuleEvent implements Serializable {

    private final String ruleName;
    private final ACSAlarm alarm;

    RuleEvent(String ruleName, ACSAlarm alarm) {
        this.ruleName = ruleName;
        this.alarm = alarm;
    }

    public String getRuleName() {
        return ruleName;
    }

    public ACSAlarm getAlarm() {
        return alarm;
    }
}
