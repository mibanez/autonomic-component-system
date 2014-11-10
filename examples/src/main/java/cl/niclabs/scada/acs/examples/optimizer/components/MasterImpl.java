package cl.niclabs.scada.acs.examples.optimizer.components;

import cl.niclabs.scada.acs.component.controllers.CommunicationException;
import cl.niclabs.scada.acs.component.controllers.Wrapper;
import cl.niclabs.scada.acs.component.controllers.utils.WrongWrapper;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;

import java.util.LinkedList;

import static cl.niclabs.scada.acs.examples.optimizer.OptimizerConfig.MAX_SLAVES;
import static cl.niclabs.scada.acs.examples.optimizer.OptimizerConfig.TOTAL_SLAVES_SLOTS;


public class MasterImpl implements Solver, BindingController {

    public static final String MASTER_SERVER_ITF = "master-server-itf";
    public static final String SLAVE_CLIENT_ITF(int i) {
        return i + "-slave-client-itf";
    }
    public static final String[] SLAVE_CLIENT_ITFS() {
        String[] interfaces = new String[TOTAL_SLAVES_SLOTS];
        for (int i = 0; i < TOTAL_SLAVES_SLOTS; i++) {
            interfaces[0] = i + SLAVE_CLIENT_ITF(i);
        }
        return interfaces;
    }

    private final Slave[] slaves = new Slave[TOTAL_SLAVES_SLOTS];


    @Override
    public Wrapper<String> solve(Task task) {

        int numberOfSlaves = getNumberOfSlaves();
        long subTaskSize = (long) Math.ceil((1.0 * (task.to - task.from + 1)) / numberOfSlaves);

        // Generate tasks
        LinkedList<Task> subTasks = new LinkedList<>();
        long counter = task.from;
        while (counter + subTaskSize < task.to) {
            subTasks.add(new Task(counter, counter + subTaskSize - 1, task.encryptedPassword));
            counter += subTaskSize;
        }
        subTasks.add(new Task(counter, task.to, task.encryptedPassword));

        // Send to executions
        LinkedList<Wrapper<String>> results = new LinkedList<>();
        for (int i = 0; i < MAX_SLAVES; i++) {
            if (slaves[i] != null) {
                results.add(slaves[i].workOn(subTasks.pop()));
            }
        }

        // Obtain results
        Wrapper<String> successResult = null;
        while(!results.isEmpty()) {
            Wrapper<String> result = results.pop();
            try {
                result.unwrap(); // force sequential
                successResult = result;
            } catch (CommunicationException ignore) {
            }
        }

        if (successResult == null) {
            return new WrongWrapper<>(new PasswordNotFoundException("Password not found on this solver"));
        }

        return successResult;
    }

    private int getNumberOfSlaves() {
        int counter = 0;
        for (int i = 0; i < MAX_SLAVES; i++) {
            if (slaves[i] != null) {
                counter++;
            }
        }
        return counter;
    }

	// BINDING CONTROLLER

	@Override
	public void bindFc(String name, Object itf) throws NoSuchInterfaceException, IllegalBindingException {
		for (int i = 0; i < TOTAL_SLAVES_SLOTS; i++) {
            if (name.equals(SLAVE_CLIENT_ITF(i))) {
                if (itf instanceof Slave) {
                    slaves[i] = (Slave) itf;
                    return;
                }
                throw new IllegalBindingException("server interface for " + name + " must be an instance of Slave");
            }
        }
        throw new NoSuchInterfaceException(name);
	}

	@Override
	public String[] listFc() {
		return SLAVE_CLIENT_ITFS();
	}

	@Override
	public Object lookupFc(String name) throws NoSuchInterfaceException {
        for (int i = 0; i < TOTAL_SLAVES_SLOTS; i++) {
            if (name.equals(SLAVE_CLIENT_ITF(i))) {
                return slaves[i];
            }
        }
        throw new NoSuchInterfaceException(name);
    }

	@Override
	public void unbindFc(String name) throws NoSuchInterfaceException {
        for (int i = 0; i < TOTAL_SLAVES_SLOTS; i++) {
            if (name.equals(SLAVE_CLIENT_ITF(i))) {
                slaves[i] = null;
                return;
            }
        }
        throw new NoSuchInterfaceException(name);
    }


}
