package cl.niclabs.scada.acs.component.controllers.monitoring.events;

import cl.niclabs.scada.acs.component.controllers.monitoring.records.*;
import org.apache.log4j.Logger;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.component.componentcontroller.AbstractPAComponentController;
import org.objectweb.proactive.core.jmx.naming.FactoryName;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.jmx.notification.RequestNotificationData;
import org.objectweb.proactive.core.jmx.util.JMXNotificationManager;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import java.io.IOException;
import java.util.Queue;

/**
 * Event Listener component for the Monitoring Framework
 *
 * This NF Component subscribes to all the interesting events that are required by the monitoring activity.
 * Each time an event is received, a record is created an sent to the Log Store.
 * The specific behaviour regarding reading/updating/writing a record is dependent on the middleware.
 * This component works as a sensor of events related to the F component it belongs to.
 *
 * This version is adapted for the GCM/ProActive case, and considers asynchronism of requests and futures.
 *
 * @author cruz
 *
 */
public class GCMPAEventListenerImpl extends AbstractPAComponentController implements GCMPAEventListener,
        NotificationListener, BindingController {

    private static final Logger logger = ProActiveLogger.getLogger("ACS");

    private RecordEventListener recordEventListener;
    private RecordStore recordStore;

    private JMXNotificationManager jmxNotificationManager;

    @Override
    public void init(UniqueID uniqueId, String runtimeURL) {

        ObjectName objectName = FactoryName.createActiveObjectName(uniqueId);
        String completeRuntimeURL = FactoryName.getCompleteUrl(runtimeURL);
        jmxNotificationManager = JMXNotificationManager.getInstance();

        try {
            //logger.debug("subscribing jmxNM to {}", objectName.getCanonicalKeyPropertyListString());
            jmxNotificationManager.subscribe(objectName, this, completeRuntimeURL);
        } catch (IOException e) {
            throw new ProActiveRuntimeException("JMX subscription for the MonitorController has failed", e);
        }
    }

    @Override
    public void stop() {
        jmxNotificationManager.kill();
    }

    @Override
    public void handleNotification(Notification notification, Object o) {

        if (notification.getType().equals(NotificationType.setOfNotifications)) {
            @SuppressWarnings("unchecked")
            Queue<Notification> notificationQueue = (Queue<Notification>) notification.getUserData();
            for (Notification n : notificationQueue) {
                this.handleNotification(n, o);
            }
            return;
        }


        if (notification.getUserData() instanceof RequestNotificationData) {
            RequestNotificationData data = (RequestNotificationData) notification.getUserData();
            long millisTime = notification.getTimeStamp()/1000;
            switch (notification.getType()) {

                case NotificationType.requestReceived:      // REQUEST_RECEIVED
                    requestReceived(data, millisTime);
                    break;
                case NotificationType.servingStarted:        // REQUEST_SERVICE_STARTED
                    requestServiceStarted(data, millisTime);
                    break;
                case NotificationType.realReplySent:        // REQUEST_SERVICE_ENDED
                    requestServiceEnded(data, millisTime);
                    break;
                case NotificationType.voidRequestServed:    // REQUEST_SERVICE_ENDED for void requests
                    requestServiceEnded(data, millisTime);
                    break;
                case NotificationType.requestSent:          // REQUEST_SENT
                    requestSent(data, millisTime);
                    break;
                case NotificationType.replyReceived:        // FUTURE_RECEIVED
                    requestResponseReceived(data, millisTime);
                    break;
                case NotificationType.realReplyReceived:    // RESPONSE_RECEIVED
                    // This happens when the reply received does not have any more Futures,
                    // therefore all the reply data is available.
                    requestResponseCompleted(data, millisTime);
                    break;

                case NotificationType.voidRequestSent:      // VOID_REQUEST_SENT
                    voidRequestSent(data, millisTime);
                    break;
            }
        }
    }

    private void requestReceived(RequestNotificationData data, long time) {
        // TODO: handle request without parent, directly called from the component instance
        try {
            IncomingRecord record = new IncomingRecord(data, time);
            recordStore.update(record);
            recordEventListener.notifyRecordEvent(RecordEvent.REQUEST_RECEIVED);
            if (logger.isTraceEnabled()) {
                //logger.trace("REQUEST RECEIVED --> {}", record);
            }
        } catch (CMTagNotFoundException ignored) {
        }
    }

    private void requestServiceStarted(RequestNotificationData data, long time) {
        try {
            IncomingRecord record = new IncomingRecord(data, 0);
            record.setServiceStartedTime(time);
            recordStore.update(record);
            recordEventListener.notifyRecordEvent(RecordEvent.REQUEST_SERVICE_STARTED);
            if (logger.isTraceEnabled()) {
                //logger.trace("REQUEST SERVICE STARTED --> {}", record);
            }
        } catch (CMTagNotFoundException ignored) {
        }
    }

    private void requestServiceEnded(RequestNotificationData data, long time) {
        try {
            IncomingRecord record = new IncomingRecord(data, 0);
            record.setServiceEndedTime(time);
            recordStore.update(record);
            recordEventListener.notifyRecordEvent(RecordEvent.REQUEST_SERVICE_ENDED);
            if (logger.isTraceEnabled()) {
                //logger.trace("REQUEST SERVICE ENDED --> {}", record);
            }
        } catch (CMTagNotFoundException ignored) {
        }
    }

    private void requestSent(RequestNotificationData data, long time) {
        try {
            OutgoingRecord record = new OutgoingRecord(data, time);
            recordStore.update(record);
            recordEventListener.notifyRecordEvent(RecordEvent.REQUEST_SENT);
            if (logger.isTraceEnabled()) {
                //logger.trace("REQUEST SENT --> {}", record);
            }
        } catch (CMTagNotFoundException ignored) {
        }
    }

    private void requestResponseReceived(RequestNotificationData data, long time) {
        try {
            OutgoingRecord record = new OutgoingRecord(data, 0);
            record.setFutureReceivedTime(time);
            recordStore.update(record);
            recordEventListener.notifyRecordEvent(RecordEvent.FUTURE_RECEIVED);
            if (logger.isTraceEnabled()) {
                //logger.trace("REQUEST RESPONSE RECEIVED --> {}", record);
            }
        } catch (CMTagNotFoundException ignored) {
        }
    }

    private void requestResponseCompleted(RequestNotificationData data, long time) {
        try {
            OutgoingRecord record = new OutgoingRecord(data, 0);
            record.setResponseReceivedTime(time);
            recordStore.update(record);
            recordEventListener.notifyRecordEvent(RecordEvent.RESPONSE_RECEIVED);
            if (logger.isTraceEnabled()) {
                //logger.trace("REQUEST RESPONSE COMPLETED --> {}", record);
            }
        } catch (CMTagNotFoundException ignored) {
        }
    }

    private void voidRequestSent(RequestNotificationData data, long time) {
        try {
            OutgoingVoidRecord record = new OutgoingVoidRecord(data, time);
            recordStore.update(record);
            recordEventListener.notifyRecordEvent(RecordEvent.VOID_REQUEST_SENT);
            if (logger.isTraceEnabled()) {
                //logger.trace("VOID REQUEST SENT --> {}", record);
            }
        } catch (CMTagNotFoundException ignored) {
        }
    }

    @Override
    public String[] listFc() {
        return new String[] { RecordStore.ITF_NAME, RecordEventListener.ITF_NAME };
    }

    @Override
    public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
        if (clientItfName.equals(RecordStore.ITF_NAME)) {
            return recordStore;
        } else if (clientItfName.equals(RecordEventListener.ITF_NAME)) {
            return recordEventListener;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    @Override
    public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
        if (clientItfName.equals(RecordStore.ITF_NAME)) {
            recordStore = (RecordStore) serverItf;
        } else if (clientItfName.equals(RecordEventListener.ITF_NAME)) {
            recordEventListener = (RecordEventListener) serverItf;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    @Override
    public void unbindFc(String clientItfName) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
        if (clientItfName.equals(RecordStore.ITF_NAME)) {
            recordStore = null;
        } else if (clientItfName.equals(RecordEventListener.ITF_NAME)) {
            recordEventListener = null;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }
}
