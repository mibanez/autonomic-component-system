package cl.niclabs.scada.acs.multiactivity.component;

import cl.niclabs.scada.acs.multiactivity.compatibility.ACSAnnotationProcessor;
import cl.niclabs.scada.acs.multiactivity.component.policy.ACSMembraneServingPolicy;
import cl.niclabs.scada.acs.multiactivity.component.policy.ACSServingPolicy;
import org.apache.log4j.Logger;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.body.ComponentBody;
import org.objectweb.proactive.core.component.control.PAGCMLifeCycleController;
import org.objectweb.proactive.core.component.control.PAMembraneController;
import org.objectweb.proactive.core.component.identity.PAComponentImpl;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.multiactivity.ServingController;
import org.objectweb.proactive.multiactivity.compatibility.CompatibilityTracker;
import org.objectweb.proactive.multiactivity.component.ComponentMultiActiveService;
import org.objectweb.proactive.multiactivity.component.policy.ComponentServingPolicy;
import org.objectweb.proactive.multiactivity.execution.RequestExecutor;
import org.objectweb.proactive.multiactivity.policy.DefaultServingPolicy;
import org.objectweb.proactive.multiactivity.policy.ServingPolicy;
import org.objectweb.proactive.multiactivity.priority.PriorityConstraint;
import org.objectweb.proactive.multiactivity.priority.PriorityManager;

import java.util.LinkedList;
import java.util.List;

/**
 * A copy of ComponentMultiActiveService,
 * this version uses the ACS version of serving policies and the acs annotation processor
 */
public class ACSMultiActiveService extends Service {

    public static final boolean LIMIT_ALL_THREADS = true;
    public static final boolean LIMIT_ACTIVE_THREADS = false;
    public static final boolean REENTRANT_SAME_THREAD = true;
    public static final boolean REENTRANT_SEPARATE_THREAD = false;
    private static final Logger logger = ProActiveLogger.getLogger("ACS");

    public int activeServes = 0;
    public LinkedList<Integer> serveHistory = new LinkedList<>();
    public LinkedList<Integer> serveTsts = new LinkedList<>();
    private RequestExecutor executor = null;

    /**
     * ACSComponentMultiActiveService that will be able to optionally use a policy,
     * and will deploy each serving request on a separate physical thread.
     * <p/>
     * This is a copy of the original ComponentMultiActiveService provided by ProActive,
     * this versions uses the ACSAnnotationProcessor instead of the original AnnotationProcessor.
     *
     * @param body The body of the active object.
     */
    public ACSMultiActiveService(Body body) {
        super(body);

        if (!((ComponentBody) body).isComponent()) {
            throw new IllegalStateException(ComponentMultiActiveService.class.getName() +
                    " can only be used with GCM components");
        }

    }

    //initializing the compatibility info and the executor
    private void init() {
        this.init(null);
    }

    private void init(List<PriorityConstraint> priorityConstraints) {

        if (executor != null) {
            return;
        }

        ACSAnnotationProcessor annotationProcessor = new ACSAnnotationProcessor(body.getReifiedObject().getClass());
        CompatibilityTracker compatibility = new CompatibilityTracker(annotationProcessor, requestQueue);

        if (priorityConstraints != null) {
            annotationProcessor.getPriorityConstraints().addAll(priorityConstraints);
        }

        executor = new RequestExecutor(body, compatibility, annotationProcessor.getPriorityConstraints());

        if (logger.isDebugEnabled()) {
            if (executor.getPriorityManager().getPriorityConstraints().size() > 0) {
                logger.debug("Priority constraints for " + body.getReifiedObject().getClass());
                logger.debug(String.valueOf(executor.getPriorityManager()));
            } else {
                logger.debug("No priority constraint defined for " + body.getReifiedObject().getClass());
            }
        }
    }

    /**
     * Service that relies on the default parallel policy to extract requests from the queue.
     *
     * @param maxActiveThreads maximum number of allowed threads inside the multi-active object
     * @param hardLimit        false if the above limit is applicable only to active (running) threads, but not the waiting ones
     * @param hostReentrant    true if re-entrant calls should be hosted on the issuer's thread
     */
    public void multiActiveServing(int maxActiveThreads, boolean hardLimit, boolean hostReentrant) {
        init();
        executor.configure(maxActiveThreads, hardLimit, hostReentrant);
        executor.execute(createServingPolicy());
    }

    /**
     * Service that relies on the default parallel policy to extract requests from the queue.
     *
     * @param priorityConstraints priority constraints to apply
     * @param maxActiveThreads    maximum number of allowed threads inside the multi-active object
     * @param hardLimit           false if the above limit is applicable only to active (running) threads, but not the waiting ones
     * @param hostReentrant       true if re-entrant calls should be hosted on the issuer's thread
     */
    public void multiActiveServing(List<PriorityConstraint> priorityConstraints, int maxActiveThreads,
                                   boolean hardLimit, boolean hostReentrant) {
        init(priorityConstraints);
        executor.configure(maxActiveThreads, hardLimit, hostReentrant);
        executor.execute(createServingPolicy());
    }

    /**
     * Service that relies on the default parallel policy to extract requests from the queue. Threads are soft-limited and re-entrance on the same thread is disabled.
     *
     * @param maxActiveThreads maximum number of allowed threads inside the multi-active object
     */
    public void multiActiveServing(int maxActiveThreads) {
        init();
        executor.configure(maxActiveThreads, false, false);
        executor.execute(createServingPolicy());

    }

    /**
     * Service that relies on the default parallel policy to extract requests from the queue. Threads are not limited and re-entrance on the same thread is disabled.
     */
    public void multiActiveServing() {
        init();
        executor.configure(Integer.MAX_VALUE, false, false);
        executor.execute(createServingPolicy());
    }

    /**
     * Creates the serving policy to use to schedule requests.
     *
     * @return The serving policy to use to schedule requests.
     */
    ServingPolicy createServingPolicy() {
        return wrapServingPolicy(new DefaultServingPolicy());
    }

    /**
     * Service that relies on a user-defined policy to extract requests from the queue.
     * <p/>
     * The specified policy is automatically wrapped by a
     * {@link ComponentServingPolicy} to handle non-functional method calls
     * properly. This wrapping behavior could be redefined (and thus removed) by
     * overriding the method
     * {@link ACSMultiActiveService#wrapServingPolicy(ServingPolicy)}.
     *
     * @param policy              Serving policy to use
     * @param priorityConstraints priority constraints to apply
     * @param maxActiveThreads    maximum number of allowed threads inside the multi-active object
     * @param hardLimit           false if the above limit is applicable only to active (running) threads, but not the waiting ones
     * @param hostReentrant       true if re-entrant calls should be hosted on the issuer's thread
     */
    public void policyServing(ServingPolicy policy, List<PriorityConstraint> priorityConstraints,
                              int maxActiveThreads, boolean hardLimit, boolean hostReentrant) {
        init(priorityConstraints);
        executor.configure(maxActiveThreads, hardLimit, hostReentrant);
        executor.execute(wrapServingPolicy(policy));
    }

    /**
     * Service that relies on a user-defined policy to extract requests from the queue.
     * Threads are soft-limited and re-entrance on the same thread is disabled.
     * <p/>
     * The specified policy is automatically wrapped by a
     * {@link ComponentServingPolicy} to handle non-functional method calls
     * properly. This wrapping behavior could be redefined (and thus removed) by
     * overriding the method
     * {@link ACSMultiActiveService#wrapServingPolicy(ServingPolicy)}.
     *
     * @param policy           Serving policy to use
     * @param maxActiveThreads maximum number of allowed threads inside the multi-active object
     */
    public void policyServing(ServingPolicy policy, int maxActiveThreads) {
        init();
        executor.configure(maxActiveThreads, false, false);
        executor.execute(wrapServingPolicy(policy));
    }

    /**
     * Service that relies on a user-defined policy to extract requests from the queue.
     * Threads are not limited and re-entrance on the same thread is disabled.
     * <p/>
     * The specified policy is automatically wrapped by a
     * {@link ComponentServingPolicy} to handle non-functional method calls
     * properly. This wrapping behavior could be redefined (and thus removed) by
     * overriding the method
     * {@link ACSMultiActiveService#wrapServingPolicy(ServingPolicy)}.
     *
     * @param policy Serving policy to use
     */
    public void policyServing(ServingPolicy policy) {
        init();
        executor.configure(Integer.MAX_VALUE, false, false);
        executor.execute(wrapServingPolicy(policy));
    }

    ServingPolicy wrapServingPolicy(ServingPolicy delegate) {
        PAComponentImpl componentImpl = ((ComponentBody) body).getPAComponentImpl();
        PAGCMLifeCycleController lifeCycleController;
//        PriorityController priorityController;
        PAMembraneController membraneController;

        try {
            lifeCycleController = Utils.getPAGCMLifeCycleController(componentImpl);
        } catch (NoSuchInterfaceException nsie) {
            throw new IllegalStateException("No life cycle controller interface, unable to"
                    + "create a serving policy for this GCM component", nsie);
        }

//        try {
//            priorityController = GCM.getPriorityController(componentImpl);
//            membraneController = Utils.getPAMembraneController(componentImpl);
//
//            return new ComponentMembranePriorityServingPolicy(delegate, lifeCycleController,
//                    priorityController, membraneController);
//        } catch (NoSuchInterfaceException nsie1) {
//            try {
//                priorityController = GCM.getPriorityController(componentImpl);
//
//                return new ComponentPriorityServingPolicy(delegate, lifeCycleController, priorityController);
//            } catch (NoSuchInterfaceException nsie2) {
//                try {
//                    membraneController = Utils.getPAMembraneController(componentImpl);
//
//                    return new ComponentMembraneServingPolicy(delegate, lifeCycleController,
//                            membraneController);
//                } catch (NoSuchInterfaceException nsie3) {
//                    return new ComponentServingPolicy(delegate, lifeCycleController);
//                }
//            }
//        }

        // PriorityController not supported for now...
        try {
            membraneController = Utils.getPAMembraneController(componentImpl);
            return new ACSMembraneServingPolicy(delegate, lifeCycleController, membraneController);
        } catch (NoSuchInterfaceException e) {
            return new ACSServingPolicy(delegate, lifeCycleController);
        }

    }

    /**
     * Returns the object through which the service's properties can be modified at run-time.
     *
     * @return serving controller
     */
    public ServingController getServingController() {
        //this init runs only once even if invoked many times
        init();
        return executor;
    }

    public PriorityManager getPriorityConstraints() {
        return executor.getPriorityManager();
    }

    public RequestExecutor getRequestExecutor() {
        return this.executor;
    }

}
