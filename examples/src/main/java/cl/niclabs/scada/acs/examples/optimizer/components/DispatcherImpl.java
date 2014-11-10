package cl.niclabs.scada.acs.examples.optimizer.components;

import cl.niclabs.scada.acs.component.controllers.CommunicationException;
import cl.niclabs.scada.acs.component.controllers.Wrapper;
import cl.niclabs.scada.acs.component.controllers.utils.WrongWrapper;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.Group;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;

import java.io.Serializable;

import static cl.niclabs.scada.acs.examples.optimizer.OptimizerConfig.MAX_SOLVERS;
import static cl.niclabs.scada.acs.examples.optimizer.OptimizerConfig.TOTAL_SOLVERS_SLOTS;

/**
 * Created by mibanez
 */

@DefineGroups({
    @Group(name = "Dispatch", selfCompatible = true),
})
public class DispatcherImpl implements Dispatcher, BindingController {

    public static final String DISPATCHER_SERVER_ITF = "dispatcher-server-itf";
    public static final String SOLVER_CLIENT_ITF(int index) {
        if (index >= 0 && index < TOTAL_SOLVERS_SLOTS) {
            return index + "-solver-client-itf";
        }
        throw new IndexOutOfBoundsException("Index \"" + index + "\" must be > 0 and < MAX_SOLVERS");
    }
    public static final String[] SOLVER_CLIENT_ITFS() {
        String[] list = new String[TOTAL_SOLVERS_SLOTS];
        for (int i = 0; i <= TOTAL_SOLVERS_SLOTS; i++) {
            list[i] = SOLVER_CLIENT_ITF(i);
        }
        return list;
    }

    // dumb object used as mutex
    private Serializable mutex = new Serializable() { };

    // a true value means that the solver is not bound or that it is bound but is busy now
    private final boolean[] busySolver = new boolean[TOTAL_SOLVERS_SLOTS];

    private final Solver[] solvers = new Solver[TOTAL_SOLVERS_SLOTS];
    private int lastFreeSolverIndex = -1;


    @MemberOf("Dispatch")
    @Override
    public Wrapper<String> dispatch(Task task) {

        int availableIndex = -1;

        System.out.println("Finding a busySolver");
        synchronized (mutex) {

            boolean allSolversBusy = true;

            System.out.println("...while all solvers busy....");
            while (allSolversBusy) {

                availableIndex = getNextFreeIndex();
                System.out.println("......next avialable index = " + availableIndex);

                if (availableIndex >= 0) {
                    System.out.println("......next avialable index = " + availableIndex);
                    busySolver[availableIndex] = true;
                    allSolversBusy = false;
                } else try {
                    System.out.println("......next avialable index = " + availableIndex);
                    mutex.wait();
                } catch (InterruptedException e) {
                    return new WrongWrapper<>(e);
                }
            }

            lastFreeSolverIndex = availableIndex;
        }

        System.out.println("Sending task to solver");
        Wrapper<String> result = solvers[availableIndex].solve(task);

        System.out.println("Waiting result");
        try {
            result.unwrap(); // force sequential
        } catch (CommunicationException e) {
            e.printStackTrace();
        } finally {
            synchronized (mutex) {
                busySolver[availableIndex] = false;
                mutex.notifyAll();
            }
            return result;
        }
    }

    private int getNextFreeIndex() {
        for (int i = (lastFreeSolverIndex + 1) % MAX_SOLVERS; i != lastFreeSolverIndex ; i = ++i % MAX_SOLVERS) {
            if (!busySolver[i]) {
                return i;
            }
        }
        if (!busySolver[lastFreeSolverIndex]) {
            return lastFreeSolverIndex;
        }
        return -1;
    }

    @Override
    public String[] listFc() {
        return SOLVER_CLIENT_ITFS();
    }

    @Override
    public Object lookupFc(String name) throws NoSuchInterfaceException {
        for (int i = 0; i < TOTAL_SOLVERS_SLOTS; i++) {
            if (name.equals(SOLVER_CLIENT_ITF(i))) {
                return solvers[i];
            }
        }
        throw new NoSuchInterfaceException(name);
    }

    @Override
    public void bindFc(String name, Object server) throws NoSuchInterfaceException, IllegalBindingException {
        for (int i = 0; i < TOTAL_SOLVERS_SLOTS; i++) {
            if (name.equals(SOLVER_CLIENT_ITF(i))) {
                if (server instanceof Solver) {
                    solvers[i] = (Solver) server;
                    return;
                }
                throw new IllegalBindingException("the server interface for " + name +" must be an instance of Solver");
            }
        }
        throw new NoSuchInterfaceException(name);
    }

    @Override
    public void unbindFc(String name) throws NoSuchInterfaceException, IllegalBindingException {
        for (int i = 0; i < TOTAL_SOLVERS_SLOTS; i++) {
            if (name.equals(SOLVER_CLIENT_ITF(i))) {
                solvers[i] = null;
                return;
            }
        }
        throw new NoSuchInterfaceException(name);
    }

}
