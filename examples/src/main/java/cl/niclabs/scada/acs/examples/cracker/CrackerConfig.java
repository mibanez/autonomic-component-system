package cl.niclabs.scada.acs.examples.cracker;


public interface CrackerConfig {

    static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyz";
    static final int DEFAULT_MAX_LENGTH = 4;
    static final int DEFAULT_N_OF_TEST = 256;
    static final long DEFAULT_DELAY = 1000;

    // GENERAL
    static final int AVG_REQUEST_TIME_SAMPLES = 5;

    // BALANCER CONFIGURATION
    static final int D_POINT_SKIPPED_REQUESTS = 5;
    static final double D_POINT_MIN_DISTANCE = 0.025;

    // PROVIDER CONFIGURATION
    static final int MAX_SLAVES = 6;
    static final long SLEEP_TIME = 60000;
    static final long MAX_RESPONSE_TIME = 200;
    static final long MIN_RESPONSE_TIME = 350;

}
