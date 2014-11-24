package tesis.monitoring.components;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;


public class ManagerImpl implements Manager, BindingController {

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
        throw new NoSuchInterfaceException(clientItfName);
    }

    @Override
    public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException {
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
    public void add(byte[] encryptedPassword) {
        cracker.crack(encryptedPassword);
    }
}
