package cl.niclabs.scada.acs.multiactivity.component.policy;

import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFilter;
import org.objectweb.proactive.core.component.control.PAGCMLifeCycleController;
import org.objectweb.proactive.multiactivity.compatibility.StatefulCompatibilityMap;
import org.objectweb.proactive.multiactivity.policy.ServingPolicy;

import java.util.ArrayList;
import java.util.List;

/**
 * A ComponentServingPolicy copy.
 * the ACS version must use ACSNFRequestFilterImpl instead of NFRequestFilterImpl
 */
public class ACSServingPolicy extends ServingPolicy {

    private final PAGCMLifeCycleController lifeCycleController;
    private final RequestFilter acsNfRequestFilter;
    protected final ServingPolicy delegate;

    /**
     * @return the delegate
     */
    public ServingPolicy getDelegate() {
        return this.delegate;
    }

    /**
     * Creates a ACSServingPolicy.
     *
     * @param delegate custom serving policy to wrap.
     * @param lifeCycleController The life cycle controller of the GCM component.
     */
    public ACSServingPolicy(ServingPolicy delegate, PAGCMLifeCycleController lifeCycleController) {
        this.delegate = delegate;
        this.lifeCycleController = lifeCycleController;
        this.acsNfRequestFilter = new ACSNFRequestFilter(lifeCycleController);
    }

    /**
     * Default GCM component scheduling policy.
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
        List<Request> reqs = compatibility.getQueueContents();
        List<Request> ret = new ArrayList<>();

        if (lifeCycleController.getFcState().equals(PAGCMLifeCycleController.STOPPED)) {
            if (compatibility.getNumberOfExecutingRequests() != 0) {
                // A NF request is already running, need to wait it terminates before
                // serving an other NF request
                return new ArrayList<>();
            }

            for (int i = 0; i < reqs.size(); i++) {
                if (this.acsNfRequestFilter.acceptRequest(reqs.get(i))) {
                    // NF request which can be served
                    addRequestToRunnableRequests(reqs, i, compatibility, ret);

                    return ret;
                }
                // F requests are ignored since the component is stopped
            }
        } else {
            for (int i = 0; i < reqs.size(); i++) {
                if (this.acsNfRequestFilter.acceptRequest(reqs.get(i))) {
                    // NF request
                    if ((compatibility.getNumberOfExecutingRequests() != 0) || (ret.size() != 0)) {
                        // Requests are already running or there is F requests before this NF request,
                        // need to wait they terminate/are served
                        return ret;
                    } else {
                        // The NF request can be served
                        addRequestToRunnableRequests(reqs, i, compatibility, ret);

                        return ret;
                    }
                } else {
                    // F request
                    i = this.runPolicyOnRequest(i, compatibility, ret);
                }
            }
        }

        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int runPolicyOnRequest(int requestIndexInRequestQueue, StatefulCompatibilityMap compatibility,
                                  List<Request> runnableRequests) {
        return this.delegate.runPolicyOnRequest(requestIndexInRequestQueue, compatibility, runnableRequests);
    }

    protected void addRequestToRunnableRequests(List<Request> queue, int requestIndex,
                                                StatefulCompatibilityMap compatibility, List<Request> runnableRequests) {
        runnableRequests.add(queue.get(requestIndex));
        compatibility.addRunning(queue.get(requestIndex));
        queue.remove(requestIndex);
    }

}
