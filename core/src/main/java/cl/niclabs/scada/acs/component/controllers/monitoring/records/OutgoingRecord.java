package cl.niclabs.scada.acs.component.controllers.monitoring.records;

import org.objectweb.proactive.core.jmx.notification.RequestNotificationData;

public class OutgoingRecord extends AbstractRecord {

    private long sentTime;
    private long futureReceivedTime;
    private long responseReceivedTime;

    public OutgoingRecord(RequestNotificationData data, long sentTime) throws CMTagNotFoundException {
        super(data);
        this.sentTime = sentTime;
        this.futureReceivedTime = 0;
        this.responseReceivedTime = 0;
    }

    @Override
    public boolean isFinished() {
        return sentTime > 0 && futureReceivedTime > 0 && responseReceivedTime > 0;
    }

    @Override
    public String toString() {
        return String.format("OutgoingRecord %s", super.toString());
    }

    public long getSentTime() {
        return sentTime;
    }

    public long getFutureReceivedTime() {
        return futureReceivedTime;
    }

    public long getResponseReceivedTime() {
        return responseReceivedTime;
    }

    public void setSentTime(long sentTime) {
        this.sentTime = sentTime;
    }

    public void setFutureReceivedTime(long futureReceivedTime) {
        this.futureReceivedTime = futureReceivedTime;
    }

    public void setResponseReceivedTime(long responseReceivedTime) {
        this.responseReceivedTime = responseReceivedTime;
    }

}
