package cl.niclabs.scada.acs.component.controllers.analysis;

/**
 * Created by mibanez
 */
public enum ACSAlarm {
    OK(0),
    WARNING(1),
    VIOLATION(2),
    ERROR(3);

    private final int level;

    private ACSAlarm(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

}
