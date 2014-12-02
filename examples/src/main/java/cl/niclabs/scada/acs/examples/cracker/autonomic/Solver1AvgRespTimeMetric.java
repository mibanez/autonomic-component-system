package cl.niclabs.scada.acs.examples.cracker.autonomic;

public class Solver1AvgRespTimeMetric extends AvgRespTimeMetric {

    protected void printMessage() {
        System.out.println("[METRICS][SOLVER0_AVG_RESPONSE_TIME] value = " + getValue());
    }
}
