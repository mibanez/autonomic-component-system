package cl.niclabs.scada.acs.component.controllers.monitoring.records;

import org.objectweb.proactive.core.jmx.notification.RequestNotificationData;

/**
 * Created by mibanez
 */
public class IncomingRecord extends Record {

    private long receptionTime;
    private long serviceStartedTime;
    private long serviceEndedTime;

    public IncomingRecord(RequestNotificationData data, long receptionTime) throws CMTagNotFoundException {
        super(data);
        this.receptionTime = receptionTime;
        this.serviceStartedTime = 0;
        this.serviceEndedTime = 0;
    }

    @Override
    public boolean isFinished() {
        return receptionTime > 0 && serviceStartedTime > 0 && serviceEndedTime > 0;
    }

    @Override
    public String toString() {
        return String.format("IncomingRecord %s", super.toString());
    }

    public long getReceptionTime() {
        return receptionTime;
    }

    public long getServiceStartedTime() {
        return serviceStartedTime;
    }

    public long getServiceEndedTime() {
        return serviceEndedTime;
    }

    public void setReceptionTime(long receptionTime) {
        this.receptionTime = receptionTime;
    }

    public void setServiceStartedTime(long serviceStartedTime) {
        this.serviceStartedTime = serviceStartedTime;
    }

    public void setServiceEndedTime(long serviceEndedTime) {
        this.serviceEndedTime = serviceEndedTime;
    }

}
