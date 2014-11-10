package cl.niclabs.scada.acs.examples.optimizer;

/**
 * Created by mibanez
 */
public class OptimizerConfig {

    public static final int MAX_SLAVES = 6;
    public static final int MAX_SOLVERS = 3;

    public static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyz";
    public static final int PASSWORD_MAX_LENGTH = 4;

    public static final long TASK_SIZE = (long) Math.pow(2, 16);


    // DO NOT CHANGE
    public static final int TOTAL_SLAVES_SLOTS = 16;
    public static final int TOTAL_SOLVERS_SLOTS = 8;

    static {
        assert(MAX_SLAVES <= TOTAL_SLAVES_SLOTS);
        assert(MAX_SOLVERS <= TOTAL_SOLVERS_SLOTS);
    }
}
