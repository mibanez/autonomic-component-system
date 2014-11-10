package cl.niclabs.scada.acs.component.controllers.monitoring.records;

import org.objectweb.proactive.core.jmx.notification.RequestNotificationData;

/**
 * Created by mibanez
 */
public class OutgoingVoidRecord extends AbstractRecord {

    private long sentTime;

    public OutgoingVoidRecord(RequestNotificationData data, long sentTime) throws CMTagNotFoundException {
        super(data);
        this.sentTime = sentTime;
    }

    @Override
    public boolean isFinished() {
        return sentTime > 0;
    }

    @Override
    public String toString() {
        return String.format("OutgoingVoidRecord %s", super.toString());
    }

    @SuppressWarnings("UnusedDeclaration")
    public long getSentTime() {
        return sentTime;
    }

}
