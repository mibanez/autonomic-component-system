package tesis.monitoring.components;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;

import java.security.MessageDigest;
import java.util.Arrays;

public class CrackerImpl implements Cracker, BindingController {

    private final String ALPHABET = "aAbBcCdDeEfFgGhHiIjJkKlLmMnNoOpPqrstuvwxyz0123456789";
    private final int PASSWORD_MAX_LENGTH = 4;

    private Repository repository;


    @Override
    public String[] listFc() {
        return new String[] { Repository.NAME };
    }

    @Override
    public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
        if (clientItfName.equals(Repository.NAME)) {
            return repository;
        }
        throw new NoSuchInterfaceException(clientItfName);
    }

    @Override
    public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException {
        if (clientItfName.equals(Repository.NAME)) {
            repository = (Repository) serverItf;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    @Override
    public void unbindFc(String clientItfName) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
        if (clientItfName.equals(Repository.NAME)) {
            repository = null;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    @Override
    public void crack(byte[] encryptedPassword) {
        long possibilities = 0;
        for (int i = 0; i <= PASSWORD_MAX_LENGTH; i++) {
            possibilities += Math.pow(ALPHABET.length(), i);
        }

        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            for(long i = 0; i <= possibilities; i++) {
                String option = convertToBase(i);
                do {
                    byte[] proposal = md5.digest(option.getBytes());
                    if (Arrays.equals(proposal, encryptedPassword)
                            && Arrays.equals(md5.digest(option.getBytes()), encryptedPassword)) {
                        repository.store(encryptedPassword, option);
                        return;
                    }
                    option = 0 + option;
                } while(option.length() <= PASSWORD_MAX_LENGTH);
            }
            repository.fail(encryptedPassword, "not in the search scope");
        } catch(Exception e) {
            repository.fail(encryptedPassword, "exception: ".concat(e.getMessage()));
        }
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
}
