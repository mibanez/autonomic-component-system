package cl.niclabs.scada.acs.examples.optimizer.components;

import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;

import java.io.Serializable;
import java.util.HashSet;

import static cl.niclabs.scada.acs.examples.optimizer.OptimizerConfig.*;


public class ManagerImpl implements Cracker, BindingController, Serializable {

    private Dispatcher dispatcher;

    @Override
    public Wrapper<String> crack(byte[] encryptedPassword) {

        long possibilities = 0;
        for (int i = 0; i <= PASSWORD_MAX_LENGTH; i++) {
            possibilities += Math.pow(ALPHABET.length(), i);
        }

        HashSet<Wrapper<String>> results = new HashSet<>();

        long counter = 0;
        for ( ; counter + TASK_SIZE < possibilities; counter += TASK_SIZE) {
            results.add(dispatcher.dispatch(new Task(counter, counter + TASK_SIZE - 1, encryptedPassword)));
        }
        results.add(dispatcher.dispatch(new Task(counter, possibilities, encryptedPassword)));

        Wrapper<String> password = null;
        for (Wrapper<String> result : results ) {

            if (result.unwrap() != null) {
                password = result;
                // no retornar de inmediato
                // forzar tiempo de ejecución iguales
            }
        }

        return password;
    }

	// BINDING CONTROLLER

	@Override
	public String[] listFc() {
		return new String[] { Dispatcher.NAME };
	}

	@Override
	public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
		if (clientItfName.equals(Dispatcher.NAME)) {
            return dispatcher;
        }
        throw new NoSuchInterfaceException(clientItfName);
	}

	@Override
	public void bindFc(String name, Object server) throws NoSuchInterfaceException, IllegalBindingException {
		if (name.equals(Dispatcher.NAME)) {
            if (server instanceof Dispatcher) {
                dispatcher = (Dispatcher) server;
            } else {
                throw new IllegalBindingException("server interface for " + name + " must be an instance of Dispatcher");
            }
        } else {
            throw new NoSuchInterfaceException(name);
        }
	}

	@Override
	public void unbindFc(String name) throws NoSuchInterfaceException {
        if (name.equals(Dispatcher.NAME)) {
            dispatcher = null;
        } else {
            throw new NoSuchInterfaceException(name);
        }
	}

}
