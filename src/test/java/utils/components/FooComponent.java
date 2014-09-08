package utils.components;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;

/**
 * Created by mibanez on 08-09-14.
 */
public class FooComponent implements FooInterface, BindingController {

    FooInterface client;

    @Override
    public int fooMethod(String fooParameter) {
        return 0;
    }

    @Override
    public String[] listFc() {
        return new String[] { "client-itf" };
    }

    @Override
    public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
        if (clientItfName.equals("client-itf")) {
            return client;
        }
        throw new NoSuchInterfaceException(clientItfName);
    }

    @Override
    public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException {
        if (clientItfName.equals("client-itf")) {
            client = (FooInterface) serverItf;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    @Override
    public void unbindFc(String clientItfName) throws NoSuchInterfaceException {
        if (clientItfName.equals("client-itf")) {
            client = null;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

}
