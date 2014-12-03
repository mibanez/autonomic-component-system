package cl.niclabs.scada.acs.examples.cracker;


public interface CrackerConfig {

    static final String ALPHABET = "0123456789aAbBcCdDeEfFgGhHiIjJkKlLmMnNoOpPqQrRsStTuUvVwWxXyYzZ";
    static final int DEFAULT_MAX_LENGTH = 4;
    static final int DEFAULT_N_OF_TEST = 1000000;
    static final long DEFAULT_DELAY = 100;

    // GENERAL

    // Indicates how many records should be used to calculate the avg response time
    static final int RECORD_SAMPLES_SIZE = 5;

    // BALANCER CONFIGURATION

    // Indicates how many times the recalculation must be skipped after a successful calculation
    static final int SKIPPED_CALCULATIONS = RECORD_SAMPLES_SIZE;
    static final double DST_POINT_MIN_DISTANCE = 0.025;

    // PROVIDER CONFIGURATION
    static final int MAX_SLAVES = 6;
    static final long SLEEP_TIME = 30000;
    static final long MAX_RESPONSE_TIME = 1000;
    static final long MIN_RESPONSE_TIME = 3000;

}
