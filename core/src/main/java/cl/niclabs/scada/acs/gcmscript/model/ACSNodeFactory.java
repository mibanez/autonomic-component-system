package cl.niclabs.scada.acs.gcmscript.model;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;

public interface ACSNodeFactory {

    MetricNode createMetricNode(Component host, String metricId) throws NoSuchInterfaceException;
    RuleNode createRuleNode(Component host, String ruleId) throws NoSuchInterfaceException;
    PlanNode createPlanNode(Component host, String planId) throws NoSuchInterfaceException;


}
