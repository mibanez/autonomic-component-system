package cl.niclabs.scada.acs.component.adl;

import cl.niclabs.scada.acs.component.ACSUtils;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.RemoteMonitoringManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.component.PAInterface;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.type.PAGCMInterfaceType;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;

import java.util.HashMap;

public class ACSAdlFactoryTest {

    @Before
    public void setUp() throws Exception {
        System.setProperty("gcm.provider", "org.objectweb.proactive.core.component.Fractive");
        System.setProperty("java.security.policy", ACSAdlFactoryTest.class.getResource("/proactive.java.policy").getPath());
    }

	@Test
    public void testFactory() {

        Component myComponent;
        try {
            ACSAdlFactory adlFactory = (ACSAdlFactory) ACSAdlFactoryFactory.getACSAdlFactory();
            myComponent = (Component) adlFactory.newACSComponent("cl.niclabs.scada.acs.component.adl.Composite", null);
            Assert.assertNotNull(ACSUtils.getMonitoringController(myComponent));
            Assert.assertNotNull(ACSUtils.getAnalysisController(myComponent));
            Assert.assertNotNull(ACSUtils.getPlanningController(myComponent));
            Assert.assertNotNull(ACSUtils.getExecutionController(myComponent));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            return;
        }

        Object[] itfs = myComponent.getFcInterfaces();
        HashMap<String, PAInterface> itfMap = new HashMap<>(itfs.length);
        for (Object itf : itfs) {
            if (itf instanceof PAInterface) {
                itfMap.put(((PAInterface) itf).getFcItfName(), (PAInterface) itf);
            }
        }

        String name = "test" + RemoteMonitoringManager.REMOTE_MONITORING_SUFFIX;
        Assert.assertTrue(itfMap.containsKey(name));
        PAGCMInterfaceType type = (PAGCMInterfaceType) itfMap.get(name).getFcItfType();
        Assert.assertEquals(PAGCMTypeFactory.SINGLETON_CARDINALITY, type.getGCMCardinality());
        Assert.assertEquals(MonitoringController.class.getName(), type.getFcItfSignature());
        Assert.assertEquals(PAGCMTypeFactory.CLIENT, type.isFcClientItf());
        Assert.assertEquals(PAGCMTypeFactory.INTERNAL, type.isInternal());

		Component master;
		try {
			master = ((PAInterface) Utils.getPABindingController(myComponent).lookupFc("test")).getFcItfOwner();
            Assert.assertNotNull(ACSUtils.getMonitoringController(master));
            Assert.assertNotNull(ACSUtils.getAnalysisController(master));
            Assert.assertNotNull(ACSUtils.getPlanningController(master));
            Assert.assertNotNull(ACSUtils.getExecutionController(master));
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
            Assert.fail(e.getMessage());
            return;
		}

        itfs = master.getFcInterfaces();
        itfMap = new HashMap<>(itfs.length);
        for (Object itf : itfs) {
            if (itf instanceof PAInterface) {
                itfMap.put(((PAInterface) itf).getFcItfName(), (PAInterface) itf);
            }
        }

        name = "slave" + RemoteMonitoringManager.REMOTE_MONITORING_SUFFIX;
        Assert.assertTrue(itfMap.containsKey(name));
        type = (PAGCMInterfaceType) itfMap.get(name).getFcItfType();
        Assert.assertEquals(PAGCMTypeFactory.SINGLETON_CARDINALITY, type.getGCMCardinality());
        Assert.assertEquals(MonitoringController.class.getName(), type.getFcItfSignature());
        Assert.assertEquals(PAGCMTypeFactory.CLIENT, type.isFcClientItf());
        Assert.assertEquals(PAGCMTypeFactory.EXTERNAL, type.isInternal());

        try {
            Component slave = ((PAInterface) Utils.getPABindingController(master).lookupFc("slave")).getFcItfOwner();
            Assert.assertNotNull(ACSUtils.getMonitoringController(slave));
            Assert.assertNotNull(ACSUtils.getAnalysisController(slave));
            Assert.assertNotNull(ACSUtils.getPlanningController(slave));
            Assert.assertNotNull(ACSUtils.getExecutionController(slave));
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
	}


}
