package cl.niclabs.scada.acs.examples.cracker.autonomic;


public class CrackerAvgRespTimeMetric extends AvgRespTimeMetric {

    @Override
    protected void printMessage() {
        System.out.println("[METRICS][CRACKER_AVG_RESPONSE_TIME] value = " + getValue());
    }
}
