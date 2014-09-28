package cl.niclabs.scada.acs.component.controllers.monitoring.records;

import org.objectweb.proactive.core.jmx.notification.RequestNotificationData;

public class OutgoingRequestRecord extends AbstractRecord {

    private long sentTime;
    private long responseReceivedTime;
    private long responseCompletedTime;

    public OutgoingRequestRecord(RequestNotificationData data, long sentTime)
            throws CMTagNotFoundException {

        super(data);
        this.sentTime = sentTime;
        this.responseReceivedTime = 0;
        this.responseCompletedTime = 0;
    }

    @Override
    public boolean isFinished() {
        return sentTime > 0 && responseReceivedTime > 0 && responseCompletedTime > 0;
    }

    @Override
    public String toString() {
        return String.format("OutgoingRequestRecord %s", super.toString());
    }

    public long getSentTime() {
        return sentTime;
    }

    public long getResponseReceivedTime() {
        return responseReceivedTime;
    }

    public long getResponseCompletedTime() {
        return responseCompletedTime;
    }

    public void setSentTime(long sentTime) {
        this.sentTime = sentTime;
    }

    public void setResponseReceivedTime(long responseReceivedTime) {
        this.responseReceivedTime = responseReceivedTime;
    }

    public void setResponseCompletedTime(long responseCompletedTime) {
        this.responseCompletedTime = responseCompletedTime;
    }

}
