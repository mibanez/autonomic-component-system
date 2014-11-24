package tesis.monitoring.components;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Matias on 23-11-2014.
 */
public class ClientImpl implements Client, BindingController {

    private Cracker cracker;

    @Override
    public String[] listFc() {
        return new String[] { Cracker.NAME };
    }

    @Override
    public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
        if (clientItfName.equals(Cracker.NAME)) {
            return cracker;
        }
        throw  new NoSuchInterfaceException(clientItfName);
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

    @Override
    public void start(int numberOfTest, int delay) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            System.out.println(">>> Running test [initial time: " + System.currentTimeMillis() + "]");

            for (int i = 1; i <= numberOfTest; i++) {
                String password = i < 10 ? String.format("xyz%d", i) : i < 100 ? String.format("yz%d", i) : String.format("z%d", i);
                cracker.crack(md5.digest(password.getBytes()));
                System.out.println(System.currentTimeMillis() + "\tSENT\t" + i);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
