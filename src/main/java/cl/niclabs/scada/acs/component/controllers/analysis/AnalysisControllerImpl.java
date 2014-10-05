package cl.niclabs.scada.acs.component.controllers.analysis;

import cl.niclabs.scada.acs.component.controllers.*;
import cl.niclabs.scada.acs.component.controllers.monitoring.MetricEvent;
import cl.niclabs.scada.acs.component.controllers.monitoring.MetricEventListener;
import cl.niclabs.scada.acs.component.controllers.utils.ValidWrapper;
import cl.niclabs.scada.acs.component.controllers.utils.WrongWrapper;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.proactive.core.component.componentcontroller.AbstractPAComponentController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Analysis Controller implementation
 *
 */
public class AnalysisControllerImpl extends AbstractPAComponentController
        implements AnalysisController, MetricEventListener, BindingController {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisController.class);

    private final HashMap<String, Rule> rules = new HashMap<>();

    private MonitoringController monitoringController;
    private RuleEventListener ruleEventListener;



    @Override
    @SuppressWarnings("unchecked")
    public Rule add(String ruleId, String className) throws DuplicatedElementIdException, InvalidElementException {
        try {
            Class<?> clazz = Class.forName(className);
            if (Rule.class.isAssignableFrom(clazz)) {
                return add(ruleId, (Class<Rule>) clazz);
            }
            throw new InvalidElementException("Can't cast " + clazz.getName() + " to " + Rule.class.getName());
        } catch (ClassNotFoundException e) {
            throw new InvalidElementException(e);
        }
    }

    @Override
    public <RULE extends Rule> Rule add(String id, Class<RULE> clazz)
            throws DuplicatedElementIdException, InvalidElementException {

        if (rules.containsKey(id)) {
            throw new DuplicatedElementIdException(id);
        } else try {
            rules.put(id, clazz.newInstance());
            return new RepresentativeRule(id, this);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new InvalidElementException(e);
        }
    }

    @Override
    public Rule get(String id) throws ElementNotFoundException {
        if (rules.containsKey(id)) {
            return new RepresentativeRule(id, this);
        }
        throw new ElementNotFoundException(id);
    }

    @Override
    public void remove(String id) throws ElementNotFoundException {
        if (rules.containsKey(id)) {
            rules.remove(id);
        } else {
            throw new ElementNotFoundException(id);
        }
    }

    @Override
    public String[] getRegisteredIds() {
        return rules.keySet().toArray(new String[rules.size()]);
    }


    @Override
    public Wrapper<ACSAlarm> verify(String id) {
        if (rules.containsKey(id)) {
            try {
                return new ValidWrapper<>(rules.get(id).verify(monitoringController));
            } catch (CommunicationException e) {
                new WrongWrapper<>(e);
            }
        }
        return new WrongWrapper<>(new ElementNotFoundException(id));
    }

    @Override
    public Wrapper<Boolean> subscribeTo(String id, String metricName) {
        if (rules.containsKey(id)) {
            try {
                rules.get(id).subscribeTo(metricName);
                return new ValidWrapper<>(true);
            } catch (CommunicationException e) {
                new WrongWrapper<>(e);
            }
        }
        return new WrongWrapper<>(new ElementNotFoundException(id));
    }

    @Override
    public Wrapper<Boolean> unsubscribeFrom(String id, String metricName) {
        if (rules.containsKey(id)) {
            try {
                rules.get(id).unsubscribeFrom(metricName);
                return new ValidWrapper<>(true);
            } catch (CommunicationException e) {
                new WrongWrapper<>(e);
            }
        }
        return new WrongWrapper<>(new ElementNotFoundException(id));
    }

    @Override
    public Wrapper<Boolean> isSubscribedTo(String id, String metricName) {
        if (rules.containsKey(id)) {
            try {
                return new ValidWrapper<>(rules.get(id).isSubscribedTo(metricName));
            } catch (CommunicationException e) {
                new WrongWrapper<>(e);
            }
        }
        return new WrongWrapper<>(new ElementNotFoundException(id));
    }

    @Override
    public void notifyUpdate(MetricEvent event) {
        for (Map.Entry<String, Rule> entry: rules.entrySet()) {
            try {
                entry.getValue().verify(monitoringController);
            } catch (CommunicationException e) {
                logger.warn("Exceptions during rules verification. Rule={}, Event={}, ExceptionMessage={}",
                        entry.getKey(), event, e.getMessage());
            }
        }
    }

    @Override
    public String[] listFc() {
        return new String[] {MONITORING_CONTROLLER_CLIENT_ITF, RULE_EVENT_LISTENER_CLIENT_ITF };
    }

    @Override
    public Object lookupFc(String name) throws NoSuchInterfaceException {
        switch (name) {
            case MONITORING_CONTROLLER_CLIENT_ITF: return monitoringController;
            case RULE_EVENT_LISTENER_CLIENT_ITF: return ruleEventListener;
            default: throw new NoSuchInterfaceException(name);
        }
    }

    @Override
    public void bindFc(String name, Object o) throws NoSuchInterfaceException {
        switch (name) {
            case MONITORING_CONTROLLER_CLIENT_ITF: monitoringController = (MonitoringController) o; break;
            case RULE_EVENT_LISTENER_CLIENT_ITF: ruleEventListener = (RuleEventListener) o; break;
            default: throw new NoSuchInterfaceException(name);
        }
    }

    @Override
    public void unbindFc(String name) throws NoSuchInterfaceException {
        switch (name) {
            case MONITORING_CONTROLLER_CLIENT_ITF: monitoringController = null; break;
            case RULE_EVENT_LISTENER_CLIENT_ITF: ruleEventListener = null; break;
            default: throw new NoSuchInterfaceException(name);
        }
    }

    public static final String CONTROLLER_NAME = "AnalysisController";
    public static final String ANALYSIS_CONTROLLER_SERVER_ITF = "analysis-controller-server-itf-nf";
    public static final String METRIC_EVENT_LISTENER_SERVER_ITF = "metric-event-listener-server-itf-nf";
    public static final String MONITORING_CONTROLLER_CLIENT_ITF = "monitoring-controller-client-itf-nf";
    public static final String RULE_EVENT_LISTENER_CLIENT_ITF = "rule-event-listener-client-itf-nf";

}
