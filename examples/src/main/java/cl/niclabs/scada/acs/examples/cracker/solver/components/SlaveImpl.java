package cl.niclabs.scada.acs.examples.cracker.solver.components;

import cl.niclabs.scada.acs.component.controllers.utils.ValidWrapper;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.component.controllers.utils.WrongWrapper;
import cl.niclabs.scada.acs.examples.cracker.solver.SolverTask;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static cl.niclabs.scada.acs.examples.cracker.dispatcher.DispatcherConfig.ALPHABET;
import static cl.niclabs.scada.acs.examples.cracker.dispatcher.DispatcherConfig.PASSWORD_MAX_LENGTH;


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

		// ALGORITHM BASED ON http://code.google.com/p/javamd5cracker/ solution
		for(long i = solverTask.getFirst(); i <= solverTask.getLast(); i++) {
			String password = decimalToAlphabet(i);
			//System.out.println("[MAIN][/CRACKER/SOLVER/WORKER] trying with: " + password);
			do {
				byte[] proposal = md5.digest(password.getBytes());
				if (Arrays.equals(proposal, solverTask.getEncryptedPassword())) {
					if (Arrays.equals(md5.digest(password.getBytes()), solverTask.getEncryptedPassword())) {
						return new ValidWrapper<>(password);
					}
				}
				password = ALPHABET.charAt(0) + password;
			} while(password.length() <= PASSWORD_MAX_LENGTH);
		}

        return new WrongWrapper<>("password not found on this worker");
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
