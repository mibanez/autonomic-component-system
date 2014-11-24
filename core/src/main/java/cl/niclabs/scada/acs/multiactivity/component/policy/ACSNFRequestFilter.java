package cl.niclabs.scada.acs.multiactivity.component.policy;

import cl.niclabs.scada.acs.component.ACSUtils;
import org.objectweb.proactive.core.body.request.RequestFilter;
import org.objectweb.proactive.core.component.body.NFRequestFilterImpl;
import org.objectweb.proactive.core.component.control.PAGCMLifeCycleController;
import org.objectweb.proactive.core.component.request.ComponentRequest;

import java.io.Serializable;

/**
 * Created by mibanez
 */
class ACSNFRequestFilter extends NFRequestFilterImpl implements RequestFilter, Serializable {

    private final PAGCMLifeCycleController lifeCycleController;

    public ACSNFRequestFilter(PAGCMLifeCycleController lifeCycleController) {
        this.lifeCycleController = lifeCycleController;
    }

    @Override
    public boolean acceptRequest(ComponentRequest request) {
        // (true -> NF request, false -> F request)

        // when life-cycle is stopped, only NF request are served, then acs controller requests should be handled as NF
        if (lifeCycleController.getFcState().equals(PAGCMLifeCycleController.STOPPED)) {
            return request.isControllerRequest();
        }

        // When life-cycle is started F requests have preference, the monitoring controller requests should be handled as F
        if (request.getMethodCall().getComponentMetadata().getComponentInterfaceName().equals(ACSUtils.MONITORING_CONTROLLER)) {
            return false;
        }

        return request.isControllerRequest();
    }

}
