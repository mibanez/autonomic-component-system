package cl.niclabs.scada.acs.examples.cracker;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ClientImpl implements Client, BindingController {

    private Cracker cracker;

    @Override
    public void start(String alphabet, int maxLength, int numberOfTest, long delay) {

        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return;
        }

        long initTime = System.currentTimeMillis();
        for (int i = 0; i < numberOfTest; i++) {
            String password = getRandomWord(alphabet, maxLength);
            long start = System.currentTimeMillis();
            String result = cracker.crack(md5.digest(password.getBytes())).unwrap();
            long end = System.currentTimeMillis();
            System.out.println("[CLIENT] " + (end - initTime) + "\t" + (end-start));
            //System.out.println("[MAIN] Original password: " + password + " crackedPassword: " + result.unwrap() + " [" + result.getMessage() + "]");
            if (delay > 0) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String getRandomWord(String alphabet, int maxLength) {
        String word = "";
        int base = alphabet.length();
        for (int i = 0; i < maxLength; i ++) {
            word += alphabet.charAt((int) (Math.random() * base));
        }
        return word;
    }


    @Override
    public String[] listFc() {
        return new String[] { Cracker.NAME };
    }

    @Override
    public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
        if (clientItfName.equals(Cracker.NAME)) {
            return cracker;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    @Override
    public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
        if (clientItfName.equals(Cracker.NAME)) {
            cracker = (Cracker) serverItf;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    @Override
    public void unbindFc(String clientItfName) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
        if (clientItfName.equals(Cracker.NAME)) {
            cracker = null;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }
}
