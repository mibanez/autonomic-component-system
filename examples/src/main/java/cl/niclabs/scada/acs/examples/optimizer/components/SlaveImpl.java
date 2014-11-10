package cl.niclabs.scada.acs.examples.optimizer.components;

import cl.niclabs.scada.acs.component.controllers.Wrapper;
import cl.niclabs.scada.acs.component.controllers.utils.ValidWrapper;
import cl.niclabs.scada.acs.component.controllers.utils.WrongWrapper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static cl.niclabs.scada.acs.examples.optimizer.OptimizerConfig.ALPHABET;
import static cl.niclabs.scada.acs.examples.optimizer.OptimizerConfig.PASSWORD_MAX_LENGTH;


public class SlaveImpl implements Slave {

	public static final String COMP_NAME = "worker-comp";

	@Override
	public Wrapper<String> workOn(Task task) {

		// ALGORITHM BASED ON http://code.google.com/p/javamd5cracker/ solution
        try {
	        MessageDigest md5 = MessageDigest.getInstance("MD5");
	        for(long i = task.from; i <= task.to; i++) {
				String option = convertToBase(i);
	            do {
	            	byte[] proposal = md5.digest(option.getBytes());
					if (Arrays.equals(proposal, task.encryptedPassword) && compare(task.encryptedPassword, option, md5)) {
						return new ValidWrapper<>(option);
					}
					option = 0 + option;
	            } while(option.length() <= PASSWORD_MAX_LENGTH);
	        }
        } catch(Exception e) {
        	e.printStackTrace();
            return new WrongWrapper<>(e);
        }
        
        return new WrongWrapper<>(new PasswordNotFoundException("password not found on this worker"));
	}

	private String convertToBase(long decimal) {
		String value = decimal == 0 ? "0" : "";  
		int mod = 0;  
		while( decimal != 0 ) {  
			mod = (int) (decimal % ALPHABET.length());
			value = ALPHABET.substring(mod, mod + 1) + value;
			decimal = decimal / ALPHABET.length();
		}
	    return value;
	}

    private boolean compare(final byte[] hash, final String option, MessageDigest md5)
    		throws NoSuchAlgorithmException {
        return Arrays.equals(md5.digest(option.getBytes()), hash);
    }
  
}
