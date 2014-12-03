package cl.niclabs.scada.acs.examples.cracker.component;

import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.examples.cracker.CrackerConfig;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ClientImpl implements Client, BindingController {

    private final String alphabet = CrackerConfig.ALPHABET;
    private Cracker cracker;

    @Override
    public void start(int maxLength, int numberOfTest, long delay) {

        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return;
        }

        System.out.println("request,time,resp_time,avg_resp_time");

        CircularFifoQueue<Long> times = new CircularFifoQueue<>(5);
        long initTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfTest; i++) {

            String password = getRandomWord(maxLength);

            long start = System.currentTimeMillis();
            Wrapper<String> result = cracker.crack(md5.digest(password.getBytes()), maxLength);
            String crackedPassword = result.unwrap();
            long end = System.currentTimeMillis();

            times.add(end - start);
            long avg = 0;
            for (long t : times) {
                avg += t;
            }
            avg /= times.size();


            System.out.println((i+1) + ","  + (end - initTime) + "," + (end-start) + "," +  avg);

            if (delay > 0) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getRandomWord(int maxLength) {
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
