package cl.niclabs.scada.acs.component.controllers.analysis;

import cl.niclabs.scada.acs.component.controllers.AnalysisController;
import cl.niclabs.scada.acs.component.controllers.MonitorController;
import cl.niclabs.scada.acs.component.controllers.monitor.MetricEvent;
import cl.niclabs.scada.acs.component.controllers.monitor.MetricEventListener;
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

    public static final String ANALYSIS_CONTROLLER_SERVER_ITF = "analysis-controller-server-itf-nf";
    public static final String METRIC_EVENT_LISTENER_SERVER_ITF = "metric-event-listener-server-itf-nf";
    public static final String MONITOR_CONTROLLER_CLIENT_ITF = "monitor-controller-client-itf-nf";

    private MonitorController monitorController;

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
            return new Wrapper<>(rules.get(name).verify(monitorController), "Alarm wrapped correctly");
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
            rule.verify(monitorController);
        }
    }

    @Override
    public String[] listFc() {
        return new String[] { MONITOR_CONTROLLER_CLIENT_ITF };
    }

    @Override
    public Object lookupFc(String name) throws NoSuchInterfaceException {
        switch (name) {
            case MONITOR_CONTROLLER_CLIENT_ITF: return monitorController;
            default: throw new NoSuchInterfaceException(name);
        }
    }

    @Override
    public void bindFc(String name, Object o) throws NoSuchInterfaceException {
        switch (name) {
            case MONITOR_CONTROLLER_CLIENT_ITF: monitorController = (MonitorController) o; break;
            default: throw new NoSuchInterfaceException(name);
        }
    }

    @Override
    public void unbindFc(String name) throws NoSuchInterfaceException {
        switch (name) {
            case MONITOR_CONTROLLER_CLIENT_ITF: monitorController = null; break;
            default: throw new NoSuchInterfaceException(name);
        }
    }
}
