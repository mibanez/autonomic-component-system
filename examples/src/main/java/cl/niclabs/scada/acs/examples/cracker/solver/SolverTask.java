package cl.niclabs.scada.acs.examples.cracker.solver;

import java.io.Serializable;

/**
 * Represents a solver task:
 * The solver must try last crack the password
 * using the words between "first" and "last".
 *
 */
public class SolverTask implements Serializable {

	private final long first;
    private final long last;
	private final byte[] encryptedPassword;

	public SolverTask(long first, long last, byte[] encryptedPassword) {
		this.first = first;
		this.last = last;
		this.encryptedPassword = encryptedPassword;
	}

	public long getFirst() {
		return first;
	}

	public long getLast() {
		return last;
	}

	public byte[] getEncryptedPassword() {
		return encryptedPassword;
	}

	public long getTotal() {
		return last - first + 1;
	}
}
