package tesis.utils;

import cl.niclabs.scada.acs.component.ACSManager;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;

/**
 *
 */
public class ComponentAnalysis {

    public static interface Foo {}
    public static class FooImpl implements Foo {}

    public static void main(String[] args) throws Exception {

        System.setProperty("java.security.policy", ACSManager.class.getResource("/proactive.java.policy").getPath());
        System.setSecurityManager(new SecurityManager());
        System.setProperty("gcm.provider", Fractive.class.getName());

        Component bootstrap = Utils.getBootstrapComponent();
        PAGCMTypeFactory tf = Utils.getPAGCMTypeFactory(bootstrap);
        PAGenericFactory gf = Utils.getPAGenericFactory(bootstrap);

        Component foo = gf.newFcInstance(tf.createFcType(new InterfaceType[]{
                tf.createGCMItfType("foo", Foo.class.getName(), false, false, "singleton")}),
                new ControllerDescription("FooComponent", "primitive"),
                new ContentDescription(FooImpl.class.getName()),
                null);
        Utils.getPAGCMLifeCycleController(foo).startFc();

        System.out.println("Done. 3 seconds...");
        Thread.sleep(3000);
        printMemory();

        foo = gf.newFcInstance(tf.createFcType(new InterfaceType[]{
                        tf.createGCMItfType("foo", Foo.class.getName(), false, false, "singleton")}),
                new ControllerDescription("FooComponent", "primitive"),
                new ContentDescription(FooImpl.class.getName()),
                null);
        Utils.getPAGCMLifeCycleController(foo).startFc();

        System.out.println("Done. 3 seconds...");
        Thread.sleep(3000);
        printMemory();

        foo = gf.newFcInstance(tf.createFcType(new InterfaceType[]{
                        tf.createGCMItfType("foo", Foo.class.getName(), false, false, "singleton")}),
                new ControllerDescription("FooComponent", "primitive"),
                new ContentDescription(FooImpl.class.getName()),
                null);
        Utils.getPAGCMLifeCycleController(foo).startFc();

        System.out.println("Done. 3 seconds...");
        Thread.sleep(3000);
        printMemory();

        foo = gf.newFcInstance(tf.createFcType(new InterfaceType[]{
                        tf.createGCMItfType("foo", Foo.class.getName(), false, false, "singleton")}),
                new ControllerDescription("FooComponent", "primitive"),
                new ContentDescription(FooImpl.class.getName()),
                null);
        Utils.getPAGCMLifeCycleController(foo).startFc();

        System.out.println("Done. 3 seconds...");
        Thread.sleep(3000);
        printMemory();
    }

    private static void printMemory() {
        int mb = 1024*1024;

        //Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();

        System.out.println("##### Heap utilization statistics [MB] #####");

        //Print used memory
        System.out.println("Used Memory:"
                + (runtime.totalMemory() - runtime.freeMemory()) / mb);

        //Print free memory
        System.out.println("Free Memory:"
                + runtime.freeMemory() / mb);

        //Print total available memory
        System.out.println("Total Memory:" + runtime.totalMemory() / mb);

        //Print Maximum available memory
        System.out.println("Max Memory:" + runtime.maxMemory() / mb);
    }
}
