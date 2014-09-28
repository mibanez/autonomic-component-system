package cl.niclabs.scada.acs.component.factory;

import cl.niclabs.scada.acs.AbstractComponentTest;
import cl.niclabs.scada.acs.component.controllers.AnalysisController;
import cl.niclabs.scada.acs.component.controllers.MonitorController;
import cl.niclabs.scada.acs.component.controllers.MonitorControllerMulticast;
import cl.niclabs.scada.acs.component.factory.exceptions.ACSFactoryException;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;

import java.util.HashSet;
import java.util.List;

/**
 * BuildHelper unit test
 * Created by mibanez
 */
public class BuildHelperTest extends AbstractComponentTest {

    @Test
    public void getAcsNfInterfaces() {

        InterfaceType[] interfaceTypes = null;
        try {
            interfaceTypes = new InterfaceType[]{
                    factory.createInterfaceType("server-itf", FooInterface.class.getName(), false, false),
                    factory.createInterfaceType("client-itf", FooInterface.class.getName(), true, true)
            };
        } catch (ACSFactoryException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        List<InterfaceType> acsNfInterfaces = null;
        try {
            acsNfInterfaces = (new BuildHelper(factory)).getAcsNfInterfaces(interfaceTypes);
        } catch (ACSFactoryException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        HashSet<String> set = new HashSet<>();
        for (InterfaceType itfType : acsNfInterfaces) {
            set.add(itfType.getFcItfName());
            boolean isMon = itfType.getFcItfSignature().equals(MonitorController.class.getName());
            boolean isMonMulticast = itfType.getFcItfSignature().equals(MonitorControllerMulticast.class.getName());
            boolean isAnalysis = itfType.getFcItfSignature().equals(AnalysisController.class.getName());
            Assert.assertTrue(isMon || isMonMulticast || isAnalysis);
        }

        Assert.assertTrue(set.contains("client-itf-external-monitor-controller"));
        Assert.assertTrue(set.contains("server-itf-internal-monitor-controller"));
        Assert.assertTrue(set.contains("internal-server-monitor-controller"));
    }

    @Test
    public void addACSControllers() {

        Component foo = null;
        try {
            ComponentType componentType = factory.createComponentType(new InterfaceType[]{
                    factory.createInterfaceType("server-itf", FooInterface.class.getName(), false, false),
                    factory.createInterfaceType("client-itf", FooInterface.class.getName(), true, true)
            });
            foo = factory.getGenericFactory().newFcInstance(componentType,
                    new ControllerDescription("FooComponent", "primitive"),
                    new ContentDescription(FooComponent.class.getName()), null);
        } catch (ACSFactoryException | InstantiationException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        BuildHelper buildHelper = null;
        try {
            buildHelper = new BuildHelper(factory);
        } catch (ACSFactoryException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        try {
            buildHelper.addObjectControllers(foo);
        } catch (ACSFactoryException e) {
            e.printStackTrace();
            Assert.fail("addObjectControllers: " + e.getMessage());
        }

        try {
            buildHelper.addACSControllers(foo);
        } catch (ACSFactoryException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        Object monitorObj = null;
        try {
            monitorObj = foo.getFcInterface("monitor-controller");
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        Assert.assertTrue(monitorObj instanceof MonitorController);
    }

}
