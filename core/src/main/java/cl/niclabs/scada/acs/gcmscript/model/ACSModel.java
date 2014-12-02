package cl.niclabs.scada.acs.gcmscript.model;

import cl.niclabs.scada.acs.gcmscript.procedures.*;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.fscript.model.Property;
import org.objectweb.proactive.extra.component.fscript.model.GCMModel;
import org.objectweb.proactive.extra.component.fscript.model.GCMProcedure;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.fractal.fscript.types.PrimitiveType.OBJECT;
import static org.objectweb.fractal.fscript.types.PrimitiveType.STRING;

/**
 * Created by mibanez
 */
public class ACSModel extends GCMModel implements ACSNodeFactory {

    @Override
    protected void createNodeKinds() {
        super.createNodeKinds();
        addKind("generic-element", new Property("name", STRING, false), new Property("state", STRING, true));
        addKind("metric", getNodeKind("generic-element"), new Property("value", OBJECT, false));
        addKind("rule", getNodeKind("generic-element"), new Property("alarm", OBJECT, false));
        addKind("plan", getNodeKind("generic-element"));
    }

    @Override
    protected void createAxes() {
        super.createAxes();
        this.addAxis(new MetricAxis(this));
        this.addAxis(new RuleAxis(this));
        this.addAxis(new PlanAxis(this));
        this.addAxis(new SubscriptionAxis(this));
        this.addAxis(new DeploymentGCMNodeAxis(this));
    }

    @Override
    protected void createAdditionalProcedures() {
        super.createAdditionalProcedures();

        List<GCMProcedure> procedures = new ArrayList<>();

        procedures.add(new AddMetricAction());
        procedures.add(new AddRuleAction());
        procedures.add(new AddPlanAction());
        procedures.add(new RemoveMetricAction());
        procedures.add(new RemoveRuleAction());
        procedures.add(new RemovePlanAction());

        procedures.add(new PrintMetricsFunction());
        procedures.add(new PrintRulesFunction());
        procedures.add(new PrintPlansFunction());

        procedures.add(new ToStringFunction());
        procedures.add(new RangeFunction());
        procedures.add(new ACSNewAction());

        for (GCMProcedure procedure : procedures) {
            try {
                procedure.bindFc(GCMProcedure.MODEL_NAME, this);
            } catch (Exception e) {
                throw new AssertionError("Internal inconsistency with " + procedure.getName() + " procedure");
            }
            addProcedure(procedure);
        }
    }

    public MetricNode createMetricNode(Component host, String metricId) throws NoSuchInterfaceException {
        return new MetricNode(this, host, metricId);
    }

    public RuleNode createRuleNode(Component host, String ruleId) throws NoSuchInterfaceException {
        return new RuleNode(this, host, ruleId);
    }

    public PlanNode createPlanNode(Component host, String planId) throws NoSuchInterfaceException {
        return new PlanNode(this, host, planId);
    }

}
