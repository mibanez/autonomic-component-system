package cl.niclabs.scada.acs.component.controllers.monitoring;

import cl.niclabs.scada.acs.AbstractComponentTest;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.IncomingRequestRecord;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.OutgoingRequestRecord;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.OutgoingVoidRequestRecord;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.RecordStore;
import cl.niclabs.scada.acs.component.factory.BuildHelper;
import cl.niclabs.scada.acs.component.factory.exceptions.ACSFactoryException;
import cl.niclabs.scada.acs.multiactivity.component.ACSMultiActiveService;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.body.ComponentRunActive;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;

import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

/**
 * ACSEventListenerTest
 *
 */
public class PAEventListenerTest extends AbstractComponentTest {

    // Special monitor notifier for testing purposes
    interface TestingMonitorNotifier extends ACSEventListener {
        long getCheckNumber();
    }

    // Special definitions to test this class
    public static interface Worker {
        @SuppressWarnings("EmptyMethod") public void init();
        public String work();
    }
    public static class Slave implements Worker {
        int counter = 0;
        public Slave() {}
        @SuppressWarnings("EmptyMethod") public void init() {}
        public String work() {
            try { Thread.sleep(100); }
            catch (InterruptedException e) { e.printStackTrace(); }
            counter++;
            return "counter = " + counter;
        }
    }
    public static class Subordinate implements Worker, BindingController {
        Worker slave;
        public Subordinate() {}
        @SuppressWarnings("EmptyMethod") public void init() {}
        public String work() {
            slave.init();
            String value = slave.work();
            try { Thread.sleep(150); }
            catch (InterruptedException e) { e.printStackTrace(); }
            return value;
        }
        public String[] listFc() { return new String[] { "slave" }; }
        public Object lookupFc(String s) throws NoSuchInterfaceException { return slave; }
        public void bindFc(String s, Object o) throws NoSuchInterfaceException { slave = (Worker) o; }
        public void unbindFc(String s) throws NoSuchInterfaceException { slave = null; }
    }
    public static class Master implements Worker, BindingController {
        Worker slave;
        public Master() {}
        public void init() {}
        public String work() {
            String value = slave.work();
            try { Thread.sleep(150); }
            catch (InterruptedException e) { e.printStackTrace(); }
            return value;
        }
        public String[] listFc() { return new String[] { "slave" }; }
        public Object lookupFc(String s) throws NoSuchInterfaceException { return slave; }
        public void bindFc(String s, Object o) throws NoSuchInterfaceException { slave = (Worker) o; }
        public void unbindFc(String s) throws NoSuchInterfaceException { slave = null; }
    }

    @Test
    public void eventListenerTest() {

        Component master = null, subordinate = null, slave = null;
        try {
            // Component type definitions
            ComponentType masterComponentType = factory.createComponentType(new InterfaceType[]{
                    factory.createInterfaceType("master", Worker.class.getName(), false, false, "singleton"),
                    factory.createInterfaceType("slave", Worker.class.getName(), true, false, "singleton")
            });
            ComponentType slaveComponentType = factory.createComponentType(new InterfaceType[]{
                    factory.createInterfaceType("slave", Worker.class.getName(), false, false, "singleton")
            });

            // Components instantiation
            PAGenericFactory genericFactory = factory.getGenericFactory();
            BuildHelper buildHelper = new BuildHelper(factory);
            master = genericFactory.newFcInstance(masterComponentType,
                    new ControllerDescription("Master", "primitive"),
                    new ContentDescription(Master.class.getName(), null, new ComponentRunActive() {
                        public void runComponentActivity(Body body) {
                            (new ACSMultiActiveService(body)).multiActiveServing();
                        }
                    }, null), null);
            buildHelper.addObjectControllers(master);
            subordinate = genericFactory.newFcInstance(masterComponentType,
                    new ControllerDescription("Subordinate", "primitive"),
                    new ContentDescription(Subordinate.class.getName(), null, new ComponentRunActive() {
                        public void runComponentActivity(Body body) {
                            (new ACSMultiActiveService(body)).multiActiveServing();
                        }
                    }, null), null);
            buildHelper.addObjectControllers(subordinate);
            slave = genericFactory.newFcInstance(slaveComponentType,
                    new ControllerDescription("Slave", "primitive"),
                    new ContentDescription(Slave.class.getName(), null, new ComponentRunActive() {
                        public void runComponentActivity(Body body) {
                            (new ACSMultiActiveService(body)).multiActiveServing();
                        }
                    }, null), null);
            buildHelper.addObjectControllers(slave);
        } catch (ACSFactoryException | InstantiationException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        // Component initialization
        try {
            Utils.getPAMembraneController(slave).startMembrane();
            Utils.getPAMembraneController(master).startMembrane();
            Utils.getPAMembraneController(subordinate).startMembrane();
            Utils.getPABindingController(subordinate).bindFc("slave", slave.getFcInterface("slave"));
            Utils.getPABindingController(master).bindFc("slave", subordinate.getFcInterface("master"));
            Utils.getPAGCMLifeCycleController(slave).startFc();
            Utils.getPAGCMLifeCycleController(subordinate).startFc();
            Utils.getPAGCMLifeCycleController(master).startFc();
        } catch (IllegalLifeCycleException | NoSuchInterfaceException | IllegalBindingException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        // Definition of fake monitor notifier
        TestingMonitorNotifier monitorNotifier = new TestingMonitorNotifier() {
            final AtomicLong checkNumber = new AtomicLong(0);
            @Override
            public void notifyACSEvent(ACSEventType eventType) {
                switch (eventType) {
                    case REQUEST_RECEIVED: checkNumber.addAndGet(1); break;
                    case REQUEST_SERVICE_STARTED: checkNumber.addAndGet(10); break;
                    case REQUEST_SERVICE_ENDED: checkNumber.addAndGet(100); break;
                    case REQUEST_SENT: checkNumber.addAndGet(1000); break;
                    case REQUEST_RESPONSE_RECEIVED: checkNumber.addAndGet(10000); break;
                    case REQUEST_RESPONSE_COMPLETED: checkNumber.addAndGet(100000); break;
                    case VOID_REQUEST_SENT: checkNumber.addAndGet(1000000); break;
                    default: checkNumber.set(-1);
                }
            }
            @Override
            public long getCheckNumber() {
                return checkNumber.get();
            }
        };

        // Definition of fake record store
        RecordStore recordStore = mock(RecordStore.class);
        doNothing().when(recordStore).update(any(IncomingRequestRecord.class));
        doNothing().when(recordStore).update(any(OutgoingRequestRecord.class));
        doNothing().when(recordStore).update(any(OutgoingVoidRequestRecord.class));

        // Definition of real ACSEventListener class to test
        String runtimeURL = ProActiveRuntimeImpl.getProActiveRuntime().getURL();
        new PAEventListener(monitorNotifier, recordStore, ((PAComponent) subordinate).getID(), runtimeURL);

        // Testing ....
        try {
            Worker worker = (Worker) master.getFcInterface("master");
            for (int i = 0; i < 9; i++) {
                worker.work();
            }
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
        }

        if (monitorNotifier.getCheckNumber() != 9999999L) {
            try {
                System.out.println("Waiting 2 seconds for delayed events...");
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Each event is expected to appear exactly one time on each call
        // Since the master work() method is called 9 times, the checkNumber
        // must be exactly 9999999.
        Assert.assertEquals(9999999L, monitorNotifier.getCheckNumber());
    }

}
