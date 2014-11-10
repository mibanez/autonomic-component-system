package cl.niclabs.scada.acs.multiactivity.component.policy;

import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.component.body.MembraneControllerRequestFilter;
import org.objectweb.proactive.core.component.control.PAGCMLifeCycleController;
import org.objectweb.proactive.core.component.control.PAMembraneController;
import org.objectweb.proactive.multiactivity.compatibility.StatefulCompatibilityMap;
import org.objectweb.proactive.multiactivity.policy.ServingPolicy;

import java.util.ArrayList;
import java.util.List;

/**
 * A copy of ComponentMembraneServingPolicy,
 * this extends ACSServingPolicy instead of ComponentServingPolicy
 */
public class ACSMembraneServingPolicy extends ACSServingPolicy {

    private final PAMembraneController membraneController;
    private final MembraneControllerRequestFilter membraneControllerRequestFilter;

    /**
     * Creates a ACSMembraneServingPolicy.
     *
     * @param delegate custom serving policy to wrap.
     * @param lifeCycleController The life cycle controller of the GCM component.
     * @param membraneController The membrane controller of the GCM component.
     */
    public ACSMembraneServingPolicy(ServingPolicy delegate, PAGCMLifeCycleController lifeCycleController,
                                          PAMembraneController membraneController) {
        super(delegate, lifeCycleController);
        this.membraneController = membraneController;
        this.membraneControllerRequestFilter = new MembraneControllerRequestFilter();
    }

    /**
     * Scheduling policy for GCM components with a membrane controller.
     * <br>
     * Non-functional requests are served one by one and for the functional
     * requests it will take a request from the queue if it is compatible with
     * all executing ones and also with everyone before it in the queue. If a
     * functional request can not be taken out from the queue, the requests it
     * is invalid with are marked accordingly so that they are not retried
     * until this one is finally served.
     *
     * @return The compatible requests to serve.
     */
    public List<Request> runPolicy(StatefulCompatibilityMap compatibility) {
        if (membraneController.getMembraneState().equals(PAMembraneController.MEMBRANE_STOPPED)) {
            List<Request> reqs = compatibility.getQueueContents();
            List<Request> ret = new ArrayList<>();

            if (compatibility.getNumberOfExecutingRequests() != 0) {
                // A NF request is already running, need to wait it terminates before
                // serving an other NF request
                return new ArrayList<>();
            }

            for (int i = 0; i < reqs.size(); i++) {
                if (this.membraneControllerRequestFilter.acceptRequest(reqs.get(i))) {
                    // NF request which can be served because it passes the membrane controller
                    // request filter
                    addRequestToRunnableRequests(reqs, i, compatibility, ret);

                    return ret;
                }
                // Requests (F or NF) that do not pass the membrane controller request filter
                // are ignored since the membrane is stopped
            }

            return ret;
        } else {
            return super.runPolicy(compatibility);
        }
    }

}
