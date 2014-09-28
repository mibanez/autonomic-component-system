package cl.niclabs.scada.acs.component.controllers.monitor;

import cl.niclabs.scada.acs.component.controllers.monitor.records.*;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.jmx.naming.FactoryName;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.jmx.notification.RequestNotificationData;
import org.objectweb.proactive.core.jmx.util.JMXNotificationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class ACSEventListener implements NotificationListener {

    private static final Logger logger = LoggerFactory.getLogger(ACSEventListener.class);
    private final MonitorNotifier monitorNotifier;
    private final RecordStore recordStore;


    public ACSEventListener(MonitorNotifier monitorNotifier, RecordStore recordStore, UniqueID uniqueId, String runtimeURL) {
        this.monitorNotifier = monitorNotifier;
        this.recordStore = recordStore;
        ObjectName objectName = FactoryName.createActiveObjectName(uniqueId);
        String completeRuntimeURL = FactoryName.getCompleteUrl(runtimeURL);
        JMXNotificationManager jmxNotificationManager = JMXNotificationManager.getInstance();

        try {
            logger.debug("subscribing jmxNM to {}", objectName.getCanonicalKeyPropertyListString());
            jmxNotificationManager.subscribe(objectName, this, completeRuntimeURL);
        } catch (IOException e) {
            throw new ProActiveRuntimeException("JMX subscription for the MonitorController has failed", e);
        }
        // logger.debug("[EventListener] Monitoring Started for component ["+ monitoredComponentName + "] "+ " bodyID: "+ monitoredBodyID + " @ "+ runtimeURL);

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
            switch (notification.getType()) {

                case NotificationType.requestReceived:      // REQUEST_RECEIVED
                    requestReceived(data, notification.getTimeStamp());
                    break;
                case NotificationType.servingStarted:        // REQUEST_SERVICE_STARTED
                    requestServiceStarted(data, notification.getTimeStamp());
                    break;
                case NotificationType.realReplySent:        // REQUEST_SERVICE_ENDED
                    requestServiceEnded(data, notification.getTimeStamp());
                    break;
                case NotificationType.voidRequestServed:    // REQUEST_SERVICE_ENDED for void requests
                    requestServiceEnded(data, notification.getTimeStamp());
                    break;
                case NotificationType.requestSent:          // REQUEST_SENT
                    requestSent(data, notification.getTimeStamp());
                    break;
                case NotificationType.replyReceived:        // REQUEST_RESPONSE_RECEIVED
                    requestResponseReceived(data, notification.getTimeStamp());
                    break;
                case NotificationType.realReplyReceived:    // REQUEST_RESPONSE_COMPLETED
                    // This happens when the reply received does not have any more Futures,
                    // therefore all the reply data is available.
                    requestResponseCompleted(data, notification.getTimeStamp());
                    break;

                case NotificationType.voidRequestSent:      // VOID_REQUEST_SENT
                    voidRequestSent(data, notification.getTimeStamp());
                    break;
            }
        }
    }

    private void requestReceived(RequestNotificationData data, long time) {
        // TODO: handle request without parent, directly called from the component instance
        try {
            IncomingRequestRecord record = new IncomingRequestRecord(data, time);
            recordStore.update(record);
            monitorNotifier.notifyACSEvent(ACSEventType.REQUEST_RECEIVED);
            if (logger.isTraceEnabled()) {
                logger.trace("REQUEST RECEIVED --> {}", record);
            }
        } catch (CMTagNotFoundException e) {
            logger.debug("CMTag not found on event {}", data);
        }
    }

    private void requestServiceStarted(RequestNotificationData data, long time) {
        try {
            IncomingRequestRecord record = new IncomingRequestRecord(data, 0);
            record.setServiceStartedTime(time);
            recordStore.update(record);
            monitorNotifier.notifyACSEvent(ACSEventType.REQUEST_SERVICE_STARTED);
            if (logger.isTraceEnabled()) {
                logger.trace("REQUEST SERVICE STARTED --> {}", record);
            }
        } catch (CMTagNotFoundException e) {
            logger.debug("CMTag not found on event {}", data);
        }
    }

    private void requestServiceEnded(RequestNotificationData data, long time) {
        try {
            IncomingRequestRecord record = new IncomingRequestRecord(data, 0);
            record.setServiceEndedTime(time);
            recordStore.update(record);
            monitorNotifier.notifyACSEvent(ACSEventType.REQUEST_SERVICE_ENDED);
            if (logger.isTraceEnabled()) {
                logger.trace("REQUEST SERVICE ENDED --> {}", record);
            }
        } catch (CMTagNotFoundException e) {
            logger.debug("CMTag not found on event {}", data);
        }
    }

    private void requestSent(RequestNotificationData data, long time) {
        try {
            OutgoingRequestRecord record = new OutgoingRequestRecord(data, time);
            recordStore.update(record);
            monitorNotifier.notifyACSEvent(ACSEventType.REQUEST_SENT);
            if (logger.isTraceEnabled()) {
                logger.trace("REQUEST SENT --> {}", record);
            }
        } catch (CMTagNotFoundException e) {
            logger.debug("CMTag not found on event {}", data);
        }
    }

    private void requestResponseReceived(RequestNotificationData data, long time) {
        try {
            OutgoingRequestRecord record = new OutgoingRequestRecord(data, 0);
            record.setResponseReceivedTime(time);
            recordStore.update(record);
            monitorNotifier.notifyACSEvent(ACSEventType.REQUEST_RESPONSE_RECEIVED);
            if (logger.isTraceEnabled()) {
                logger.trace("REQUEST RESPONSE RECEIVED --> {}", record);
            }
        } catch (CMTagNotFoundException e) {
            logger.debug("CMTag not found on event {}", data);
        }
    }

    private void requestResponseCompleted(RequestNotificationData data, long time) {
        try {
            OutgoingRequestRecord record = new OutgoingRequestRecord(data, 0);
            record.setResponseCompletedTime(time);
            recordStore.update(record);
            monitorNotifier.notifyACSEvent(ACSEventType.REQUEST_RESPONSE_COMPLETED);
            if (logger.isTraceEnabled()) {
                logger.trace("REQUEST RESPONSE COMPLETED --> {}", record);
            }
        } catch (CMTagNotFoundException e) {
            logger.debug("CMTag not found on event {}", data);
        }
    }

    private void voidRequestSent(RequestNotificationData data, long time) {
        try {
            OutgoingVoidRequestRecord record = new OutgoingVoidRequestRecord(data, time);
            recordStore.update(record);
            monitorNotifier.notifyACSEvent(ACSEventType.VOID_REQUEST_SENT);
            if (logger.isTraceEnabled()) {
                logger.trace("VOID REQUEST SENT --> {}", record);
            }
        } catch (CMTagNotFoundException e) {
            logger.debug("CMTag not found on event {}", data);
        }
    }

}
