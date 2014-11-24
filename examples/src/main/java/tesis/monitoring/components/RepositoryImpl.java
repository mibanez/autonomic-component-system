package tesis.monitoring.components;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;

import java.util.HashMap;

public class RepositoryImpl implements Repository, BindingController {

    HashMap<String, byte[]> passwords = new HashMap<>();

    @Override
    public void store(byte[] encryptedPassword, String password) {
        passwords.put(password, encryptedPassword);
        System.out.println(System.currentTimeMillis() + "\tRECEIVED\t" + passwords.size());
    }

    @Override
    public void fail(byte[] encryptedPassword, String msg) {
        System.out.printf(">>> Fail to calculate password for %s: %s\n", encryptedPassword.toString(), msg);
    }

    @Override
    public String[] listFc() {
        return new String[0];
    }

    @Override
    public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
        return null;
    }

    @Override
    public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException {
        throw new NoSuchInterfaceException("shouldn't happen");
    }

    @Override
    public void unbindFc(String clientItfName) throws NoSuchInterfaceException {
        throw new NoSuchInterfaceException("shouldn't happen");
    }
}
