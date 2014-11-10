package cl.niclabs.scada.acs.examples.optimizer.components;

import java.io.Serializable;

public class Task implements Serializable {

	final long from;
    final long to;
	final byte[] encryptedPassword;

	public Task(long from, long to, byte[] encryptedPassword) {
		this.from = from;
		this.to = to;
		this.encryptedPassword = encryptedPassword;
	}

}
