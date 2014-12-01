package cl.niclabs.scada.acs.examples.cracker.common.components;

import cl.niclabs.scada.acs.component.controllers.utils.ValidWrapper;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.component.controllers.utils.WrongWrapper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static cl.niclabs.scada.acs.examples.cracker.common.CrackerConfig.ALPHABET;


public class SlaveImpl implements Slave {

	private MessageDigest md5 = null;
	@Override
	public Wrapper<String> workOn(SolverTask solverTask) {

		if (md5 == null) {
			try {
				md5 = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				return new WrongWrapper<>("Can't init slave: " + e.getMessage());
			}
		}

		String validPassword = null;
		// ALGORITHM BASED ON http://code.google.com/p/javamd5cracker/ solution
		for(long i = solverTask.getFirst(); i <= solverTask.getLast(); i++) {
			String password = decimalToAlphabet(i);
			do {
				byte[] proposal = md5.digest(password.getBytes());
				if (Arrays.equals(proposal, solverTask.getEncryptedPassword())) {
					if (Arrays.equals(md5.digest(password.getBytes()), solverTask.getEncryptedPassword())) {
						validPassword = password;
						// Do not return, i want to have similar performance on every slave
					}
				}
				password = ALPHABET.charAt(0) + password;
			} while(password.length() <= solverTask.getMaxLength());
		}

		return validPassword != null ? new ValidWrapper<>(validPassword) :
				new WrongWrapper<String>("password not found on this worker");
	}

	private String decimalToAlphabet(long decimal) {
		String value = decimal == 0 ? ALPHABET.substring(0, 1) : "";
		for (int mod; decimal != 0; decimal /= ALPHABET.length()) {
			mod = (int) (decimal % ALPHABET.length());
			value = ALPHABET.substring(mod, mod + 1) + value;
		}
	    return value;
	}
}
