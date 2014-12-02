package cl.niclabs.scada.acs.examples.cracker.solver.component;

import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;

import java.io.Serializable;

/**
 * Solves a solverTask
 *
 */
public interface Solver {

    static final String NAME = "solver-itf";


    Wrapper<String> solve(SolverTask solverTask);


    class SolverTask implements Serializable {

        private final long first;
        private final long last;
        private final byte[] encryptedPassword;
        private final int maxLength;

        public SolverTask(long first, long last, byte[] encryptedPassword, int maxLength) {
            this.first = first;
            this.last = last;
            this.encryptedPassword = encryptedPassword;
            this.maxLength = maxLength;
        }

        public long getFirst() {
            return first;
        }

        public long getLast() {
            return last;
        }

        public long getTotal() {
            return last - first + 1;
        }

        public byte[] getEncryptedPassword() {
            return encryptedPassword;
        }

        public int getMaxLength() {
            return maxLength;
        }
    }
}
