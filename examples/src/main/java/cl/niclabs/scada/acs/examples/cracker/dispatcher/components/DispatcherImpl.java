package cl.niclabs.scada.acs.examples.cracker.dispatcher.components;

import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.component.controllers.utils.WrongWrapper;
import cl.niclabs.scada.acs.examples.cracker.Cracker;
import cl.niclabs.scada.acs.examples.cracker.dispatcher.DispatcherConfig;
import cl.niclabs.scada.acs.examples.cracker.solver.Solver;
import cl.niclabs.scada.acs.examples.cracker.solver.SolverTask;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class DispatcherImpl implements Cracker, BindingController {

    public static final int SOLVER_SLOTS = 8;
    private static final int PASSWORD_MAX_LENGTH = DispatcherConfig.PASSWORD_MAX_LENGTH;
    private static final String ALPHABET = DispatcherConfig.ALPHABET;

    private Map<String, Solver> solvers = new HashMap<>();


    @Override
    public Wrapper<String> crack(byte[] encryptedPassword) {

        final BlockingQueue<Wrapper<String>> results = new LinkedBlockingQueue<>(SOLVER_SLOTS);
        final Queue<Solver> solverQueue = new LinkedList<>();
        solverQueue.addAll(solvers.values());

        long passwords = 0;
        for (int i = 0; i <= PASSWORD_MAX_LENGTH; i++) {
            passwords += Math.pow(ALPHABET.length(), i);
        }

        long taskSize = Math.floorDiv(passwords, solvers.size());
        long excess = passwords - taskSize * solvers.size();

        for (int i = 0; i < solvers.size(); i++) {
            long first = taskSize * i + (i <= excess ? i : excess);
            long last = taskSize * (i + 1) - 1 + (i + 1 <= excess ? i : excess);
            SolverTask task = new SolverTask(first, last, encryptedPassword);
            //System.out.println("[MAIN][/CRACKER/DISPATCHER] sending task from: " + first + " to: " + last);
            getResultWaiter(solverQueue.remove(), task, results).start();
        }

        Wrapper<String> password = new WrongWrapper<>("Password not found");
        for (int i = 0; i < solvers.size(); i++) {
            try {
                Wrapper<String> result = results.take();
                if (result.isValid()) {
                    password = result;
                }
            } catch (InterruptedException e) {
                System.err.println("Interrupted while waiting Solver response: " + e.getMessage());
            }
        }

        return password;
    }

    private Thread getResultWaiter(final Solver solver, final SolverTask task, final Queue<Wrapper<String>> results) {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                Wrapper<String> result = solver.solve(task);
                //System.out.println("[MAIN][/CRACKER/DISPATCHER] result received: " + result.unwrap() + ": " + result.getMessage());
                if (result.isValid()) {
                    results.add(result);
                } else {
                    results.add(new WrongWrapper<String>("Solver message: " + result.getMessage()));
                }
            }
        });
    }

    @Override
    public String[] listFc() {
        String[] clients = new String[SOLVER_SLOTS];
        for (int i = 0; i < SOLVER_SLOTS; i++) {
            clients[i] = Solver.NAME.concat("-").concat(String.valueOf(i));
        }
        return clients;
    }

    @Override
    public Object lookupFc(String name) throws NoSuchInterfaceException {
        if (validClientName(name)) {
            return solvers.get(name);
        }
        throw new NoSuchInterfaceException(name);
    }

    @Override
    public void bindFc(String name, Object server) throws NoSuchInterfaceException, IllegalBindingException {
        if (validClientName(name)) {
            solvers.put(name, (Solver) server);
        } else {
            throw new NoSuchInterfaceException(name);
        }
    }

    @Override
    public void unbindFc(String name) throws NoSuchInterfaceException, IllegalBindingException {
        if (validClientName(name)) {
            solvers.remove(name);
        } else {
            throw new NoSuchInterfaceException(name);
        }
    }

    private boolean validClientName(String name) {
        if (name.startsWith(Solver.NAME)) {
            String tag = name.substring(Solver.NAME.length());
            if (tag.startsWith("-")) {
                int slot = Integer.parseInt(tag.substring(1));
                if (slot >= 0 && slot < SOLVER_SLOTS) {
                    return true;
                }
            }
        }
        return false;
    }
}
