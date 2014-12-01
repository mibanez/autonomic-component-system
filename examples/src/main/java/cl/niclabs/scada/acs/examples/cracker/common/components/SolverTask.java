package cl.niclabs.scada.acs.examples.cracker.common.components;

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
