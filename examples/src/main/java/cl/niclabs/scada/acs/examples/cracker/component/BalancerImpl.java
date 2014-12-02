package cl.niclabs.scada.acs.examples.cracker.component;

import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.component.controllers.utils.WrongWrapper;
import cl.niclabs.scada.acs.examples.cracker.CrackerConfig;
import cl.niclabs.scada.acs.examples.cracker.solver.component.Solver;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;

import java.util.LinkedList;


public class BalancerImpl implements Cracker, BalancerAttributes, BindingController {

    private static final String ALPHABET = CrackerConfig.ALPHABET;

    private Solver solver1, solver2, solver3;
    private double x = 1.0/3.0, y = 2*x;


    @Override
    public Wrapper<String> crack(byte[] encryptedPassword, int maxLength) {

        final LinkedList<Wrapper<String>> resultsQueue = new LinkedList<>();

        long passwords = 0;
        for (int i = 0; i <= maxLength; i++) {
            passwords += Math.pow(ALPHABET.length(), i);
        }

        // SOLVER1
        long start = 0;
        long end = (long) Math.floor(passwords * x);
        resultsQueue.add(solver1.solve(new Solver.SolverTask(start, end, encryptedPassword, maxLength)));

        // SOLVER2
        start = end + 1;
        end = (long) Math.floor(passwords * y);
        resultsQueue.add(solver2.solve(new Solver.SolverTask(start, end, encryptedPassword, maxLength)));

        // SOLVER3
        start = end + 1;
        end = passwords;
        resultsQueue.add(solver3.solve(new Solver.SolverTask(start, end, encryptedPassword, maxLength)));

        Wrapper<String> password = new WrongWrapper<>("Password not found");
        while (!resultsQueue.isEmpty()) {
            Wrapper<String> result = resultsQueue.poll();
            if (result.isValid()) {
                password = result;
            }
        }

        return password;
    }

    @Override
    public void setX(double x) {
        this.x = x;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public void setY(double y) {
        this.y = y;
    }

    @Override
    public double getY() {
        return y;
    }


    @Override
    public String[] listFc() {
        String[] clients = new String[3];
        for (int i = 0; i < 3; i++) {
            clients[i] = Solver.NAME + "-" + i;
        }
        return clients;
    }

    @Override
    public Object lookupFc(String name) throws NoSuchInterfaceException {
        if (name.equals(Solver.NAME + "-0")) {
            return solver1;
        } else if (name.equals(Solver.NAME + "-1")) {
            return solver2;
        } else if (name.equals(Solver.NAME + "-2")) {
            return solver3;
        }
        throw new NoSuchInterfaceException("itf not found on Balancer: " + name);
    }

    @Override
    public void bindFc(String name, Object server) throws NoSuchInterfaceException, IllegalBindingException {
        if (name.equals(Solver.NAME + "-0")) {
            solver1 = (Solver) server;
        } else if (name.equals(Solver.NAME + "-1")) {
            solver2 = (Solver) server;
        } else if (name.equals(Solver.NAME + "-2")) {
            solver3 = (Solver) server;
        } else {
            throw new NoSuchInterfaceException("itf not found on Balancer: " + name);
        }
    }

    @Override
    public void unbindFc(String name) throws NoSuchInterfaceException, IllegalBindingException {
        if (name.equals(Solver.NAME + "-0")) {
            solver1 = null;
        } else if (name.equals(Solver.NAME + "-1")) {
            solver2 = null;
        } else if (name.equals(Solver.NAME + "-2")) {
            solver3 = null;
        } else {
            throw new NoSuchInterfaceException("itf not found on Balancer: " + name);
        }
    }
}
