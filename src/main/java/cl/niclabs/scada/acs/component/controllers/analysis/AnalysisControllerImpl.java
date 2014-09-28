package cl.niclabs.scada.acs.component.controllers.analysis;

import cl.niclabs.scada.acs.component.controllers.AnalysisController;
import cl.niclabs.scada.acs.component.controllers.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.monitoring.MetricEvent;
import cl.niclabs.scada.acs.component.controllers.monitoring.MetricEventListener;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.proactive.core.component.componentcontroller.AbstractPAComponentController;

import java.util.HashMap;

/**
 * Analysis Controller implementation
 *
 */
public class AnalysisControllerImpl extends AbstractPAComponentController
        implements AnalysisController, MetricEventListener, BindingController {

    public static final String CONTROLLER_NAME = "AnalysisController";
    public static final String ANALYSIS_CONTROLLER_SERVER_ITF = "analysis-controller-server-itf-nf";
    public static final String METRIC_EVENT_LISTENER_SERVER_ITF = "metric-event-listener-server-itf-nf";
    public static final String MONITORING_CONTROLLER_CLIENT_ITF = "monitoring-controller-client-itf-nf";
    public static final String RULE_EVENT_LISTENER_CLIENT_ITF = "rule-event-listener-client-itf-nf";

    private MonitoringController monitoringController;
    private RuleEventListener ruleEventListener;

    private final HashMap<String, Rule> rules = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public Wrapper<Boolean> addRule(String name, String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (Rule.class.isAssignableFrom(clazz)) {
                return addRule(name, (Class<Rule>) clazz);
            }
            return new Wrapper<>(false, "Rule class is not assignable from the found class " + clazz.getName());
        }
        catch (Exception e) {
            return new Wrapper<>(false, "Fail to get rule class " + className, e);
        }
    }

    @Override
    public <RULE extends Rule> Wrapper<Boolean> addRule(String name, Class<RULE> clazz) {
        try {
            if (rules.containsKey(name)) {
                return new Wrapper<>(false, "The rule name " + name + " already exists");
            }
            rules.put(name, clazz.newInstance());
            return new Wrapper<>(true, "Rule " + name + " added correctly");
        }
        catch (Exception e) {
            return new Wrapper<>(false, "Fail to instantiate rule " + clazz.getName(), e);
        }
    }

    @Override
    public Wrapper<Boolean> removeRule(String name) {
        if (rules.containsKey(name)) {
            rules.remove(name);
            return new Wrapper<>(true);
        }
        return new Wrapper<>(false, "no rule with name " + name + " name");
    }

    @Override
    public Wrapper<ACSAlarm> verify(String name) {
        if (rules.containsKey(name)) {
            return new Wrapper<>(rules.get(name).verify(monitoringController), "Alarm wrapped correctly");
        }
        return new Wrapper<>(null, "No rule found with name " + name);
    }

    @Override
    public Wrapper<String[]> getRuleNames() {
        return new Wrapper<>(rules.keySet().toArray(new String[rules.size()]));
    }

    @Override
    public void notifyUpdate(MetricEvent event) {
        for (Rule rule: rules.values()) {
            rule.verify(monitoringController);
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

}
