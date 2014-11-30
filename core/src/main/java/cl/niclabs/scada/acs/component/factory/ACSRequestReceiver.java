package cl.niclabs.scada.acs.component.factory;

import cl.niclabs.scada.acs.component.ACSManager;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.ft.protocols.FTManager;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestReceiverImpl;
import org.objectweb.proactive.core.component.request.ComponentRequest;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class ACSRequestReceiver extends RequestReceiverImpl {

    AtomicInteger inACSImmediateService = new AtomicInteger(0);

    @Override
    public int receiveRequest(Request request, Body bodyReceiver) {
        if (request instanceof ComponentRequest) {
            String interfaceName = request.getMethodCall().getComponentMetadata().getComponentInterfaceName();
            if (interfaceName.equals(ACSManager.MONITORING_CONTROLLER)) {
                if (request.getMethodName().equals("getValue")) {
                    this.inACSImmediateService.incrementAndGet();
                    try {
                        bodyReceiver.serve(request);
                    } finally {
                        this.inACSImmediateService.decrementAndGet();
                    }
                    //Dummy value for immediate services...
                    return FTManager.IMMEDIATE_SERVICE;
                }
            }
        }
        return super.receiveRequest(request, bodyReceiver);
    }

    public boolean isInImmediateService() throws IOException {
        return super.isInImmediateService() || inACSImmediateService.intValue() > 0;
    }

}
