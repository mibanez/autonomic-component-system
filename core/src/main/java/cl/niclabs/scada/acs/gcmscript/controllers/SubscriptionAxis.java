package cl.niclabs.scada.acs.gcmscript.controllers;

import cl.niclabs.scada.acs.component.controllers.*;
import cl.niclabs.scada.acs.gcmscript.ACSModel;
import cl.niclabs.scada.acs.gcmscript.controllers.analysis.RuleNode;
import cl.niclabs.scada.acs.gcmscript.controllers.monitoring.MetricNode;
import cl.niclabs.scada.acs.gcmscript.controllers.planning.PlanNode;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.fscript.model.AbstractAxis;
import org.objectweb.fractal.fscript.model.Node;
import org.objectweb.fractal.fscript.model.fractal.FractalModel;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by mibanez
 */
public class SubscriptionAxis extends AbstractAxis {

    public SubscriptionAxis(FractalModel model) {
        super(model, "subscription", "generic-element", "generic-element");
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public boolean isModifiable() {
        return true;
    }

    @Override
    public Set<Node> selectFrom(Node source) {

        if (source instanceof RuleNode) {
            return selectFromRule((RuleNode) source);
        } else if (source instanceof PlanNode) {
            return selectFromPlan((PlanNode) source);
        } else {
            return new HashSet<>();
        }
    }

    private HashSet<Node> selectFromRule(RuleNode ruleNode) {

        HashSet<Node> metricNodes = new HashSet<>();
        Component host = ruleNode.getElementProxy().getHost();

        try {
            for (String metricId : ruleNode.getElementProxy().getSubscriptions()) {
                metricNodes.add(new MetricNode((ACSModel) getModel(), new MetricProxy(metricId, host)));
            }
        } catch (CommunicationException ignore) {
        }

        return metricNodes;
    }

    private Set<Node> selectFromPlan(PlanNode planNode) {

        HashSet<Node> ruleNodes = new HashSet<>();
        Component host = planNode.getElementProxy().getHost();

        try {
            for (PlanningController.AlarmSubscription alarmSubscription : planNode.getElementProxy().getSubscriptions()) {
                ruleNodes.add(new RuleNode((ACSModel) getModel(), new RuleProxy(alarmSubscription.getRuleId(), host)));
            }
        } catch (CommunicationException ignore) {
        }

        return ruleNodes;
    }

    @Override
    public void connect(Node source, Node dest) {
        try {
            if ((source instanceof RuleNode) && (dest instanceof MetricNode)) {
                ((RuleNode) source).getElementProxy().subscribeTo(((MetricNode) dest).getElementProxy().getId());
            } else if ((source instanceof PlanNode) && (dest instanceof RuleNode)) {
                ((PlanNode) source).getElementProxy().subscribeTo(((RuleNode) dest).getElementProxy().getId(), ACSAlarm.ERROR);
            } else {
                throw new IllegalArgumentException("Operation not supported for for given source and destination nodes");
            }
        } catch (CommunicationException e) {
            throw new UnsupportedOperationException("can't connect nodes: " + e.getMessage());
        }
    }

    /**
     * Removes the arc in the underlying model connecting the given source and destination
     * nodes with this axis.
     *
     * @param source
     *            the source node of the arc to remove.
     * @param dest
     *            the destination node of the arc to remove.
     * @throws UnsupportedOperationException
     *             if this axis does not support direct manipulation of its arcs.
     * @throws IllegalArgumentException
     *             if it is not possible to remove the requested arc or if it does not
     *             exist.
     */
    @Override
    public void disconnect(Node source, Node dest) {
        try {
            if ((source instanceof RuleNode) && (dest instanceof MetricNode)) {
                ((RuleNode) source).getElementProxy().unsubscribeFrom(((MetricNode) dest).getElementProxy().getId());
            } else if ((source instanceof PlanNode) && (dest instanceof RuleNode)) {
                ((PlanNode) source).getElementProxy().unsubscribeFrom(((RuleNode) dest).getElementProxy().getId());
            } else {
                throw new IllegalArgumentException("Operation not supported for for given source and destination nodes");
            }
        } catch (CommunicationException e) {
            throw new UnsupportedOperationException("can't disconnect nodes: " + e.getMessage());
        }
    }
}
