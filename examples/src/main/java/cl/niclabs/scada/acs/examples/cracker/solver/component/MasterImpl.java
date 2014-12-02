package cl.niclabs.scada.acs.examples.cracker.solver.component;

import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.component.controllers.utils.WrongWrapper;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;

import java.util.ArrayList;
import java.util.List;


public class MasterImpl implements MasterAttributes, Solver, BindingController {

    private SlaveMulticast slaves;
    private int partitionsNumber = 1;

    @Override
    public void setPartitionsNumber(double number) {
        partitionsNumber = (new Double(number).intValue());
    }

    @Override
    public double getPartitionsNumber() {
        return partitionsNumber;
    }

    @Override
    public Wrapper<String> solve(SolverTask task) {

        List<SolverTask> subTasks = new ArrayList<>();

        long subTaskSize = task.getTotal() / partitionsNumber;
        long excess = task.getTotal() - subTaskSize * partitionsNumber;

        for (int i = 0; i < partitionsNumber; i++) {
            long first = task.getFirst() + subTaskSize * i + (i <= excess ? i : excess);
            long last = task.getFirst() + subTaskSize * (i + 1) - 1 + (i + 1 <= excess ? i : excess);
            //System.out.println("[MAIN][/CRACKER/SOLVER/MASTER] sending subtask from: " + first + " to: " + last);
            subTasks.add(new SolverTask(first, last, task.getEncryptedPassword(), task.getMaxLength()));
        }

        // Sending tasks
        List<Wrapper<String>> results = slaves.workOn(subTasks);

        // Receiving results
        Wrapper<String> password = null;
        for (Wrapper<String> result: results) {
            //System.out.println("[MAIN][/CRACKER/SOLVER/MASTER] result received with message: " + result.getMessage());
            if (result.isValid()) {
                password = result;
            }
        }

        return password != null ? password : new WrongWrapper<String>("Password not found");
    }


	// BINDING CONTROLLER

	@Override
	public void bindFc(String name, Object itf) throws NoSuchInterfaceException, IllegalBindingException {
		if (name.equals(SlaveMulticast.NAME)) {
            slaves = (SlaveMulticast) itf;
        } else {
            throw new NoSuchInterfaceException(name);
        }
	}

	@Override
	public String[] listFc() {
		return new String[] { SlaveMulticast.NAME };
	}

	@Override
	public Object lookupFc(String name) throws NoSuchInterfaceException {
        if (name.equals(SlaveMulticast.NAME)) {
            return slaves;
        } else {
            throw new NoSuchInterfaceException(name);
        }
    }

	@Override
	public void unbindFc(String name) throws NoSuchInterfaceException {
        if (name.equals(SlaveMulticast.NAME)) {
            slaves = null;
        } else {
            throw new NoSuchInterfaceException(name);
        }
    }
}
