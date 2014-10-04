package cl.niclabs.scada.acs.component.adl;

import cl.niclabs.scada.acs.component.ACSUtils;
import cl.niclabs.scada.acs.component.controllers.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringControllerImpl;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.component.PAInterface;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.type.PAGCMInterfaceType;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;

import java.util.HashMap;

import static org.junit.Assert.*;

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
            assertNotNull(ACSUtils.getMonitoringController(myComponent));
            assertNotNull(ACSUtils.getAnalysisController(myComponent));
            assertNotNull(ACSUtils.getPlanningController(myComponent));
            assertNotNull(ACSUtils.getExecutionController(myComponent));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
            return;
        }

        Object[] itfs = myComponent.getFcInterfaces();
        HashMap<String, PAInterface> itfMap = new HashMap<>(itfs.length);
        for (Object itf : itfs) {
            if (itf instanceof PAInterface) {
                itfMap.put(((PAInterface) itf).getFcItfName(), (PAInterface) itf);
            }
        }

        String name = "test" + MonitoringControllerImpl.INTERNAL_MONITORING_ITF;
        assertTrue(itfMap.containsKey(name));
        PAGCMInterfaceType type = (PAGCMInterfaceType) itfMap.get(name).getFcItfType();
        assertEquals(PAGCMTypeFactory.SINGLETON_CARDINALITY, type.getGCMCardinality());
        assertEquals(MonitoringController.class.getName(), type.getFcItfSignature());
        assertEquals(PAGCMTypeFactory.CLIENT, type.isFcClientItf());
        assertEquals(PAGCMTypeFactory.INTERNAL, type.isInternal());

        name = MonitoringControllerImpl.INTERNAL_SERVER_MONITORING_ITF;
        assertTrue(itfMap.containsKey(name));
        type = (PAGCMInterfaceType) itfMap.get(name).getFcItfType();
        assertEquals(PAGCMTypeFactory.SINGLETON_CARDINALITY, type.getGCMCardinality());
        assertEquals(MonitoringController.class.getName(), type.getFcItfSignature());
        assertEquals(PAGCMTypeFactory.SERVER, type.isFcClientItf());
        assertEquals(PAGCMTypeFactory.INTERNAL, type.isInternal());


		Component master;
		try {
			master = ((PAInterface) Utils.getPABindingController(myComponent).lookupFc("test")).getFcItfOwner();
            assertNotNull(ACSUtils.getMonitoringController(master));
            assertNotNull(ACSUtils.getAnalysisController(master));
            assertNotNull(ACSUtils.getPlanningController(master));
            assertNotNull(ACSUtils.getExecutionController(master));
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
            fail(e.getMessage());
            return;
		}

        itfs = master.getFcInterfaces();
        itfMap = new HashMap<>(itfs.length);
        for (Object itf : itfs) {
            if (itf instanceof PAInterface) {
                itfMap.put(((PAInterface) itf).getFcItfName(), (PAInterface) itf);
            }
        }

        name = "slave" + MonitoringControllerImpl.EXTERNAL_MONITORING_ITF;
        assertTrue(itfMap.containsKey(name));
        type = (PAGCMInterfaceType) itfMap.get(name).getFcItfType();
        assertEquals(PAGCMTypeFactory.SINGLETON_CARDINALITY, type.getGCMCardinality());
        assertEquals(MonitoringController.class.getName(), type.getFcItfSignature());
        assertEquals(PAGCMTypeFactory.CLIENT, type.isFcClientItf());
        assertEquals(PAGCMTypeFactory.EXTERNAL, type.isInternal());

        try {
            Component slave = ((PAInterface) Utils.getPABindingController(master).lookupFc("slave")).getFcItfOwner();
            assertNotNull(ACSUtils.getMonitoringController(slave));
            assertNotNull(ACSUtils.getAnalysisController(slave));
            assertNotNull(ACSUtils.getPlanningController(slave));
            assertNotNull(ACSUtils.getExecutionController(slave));
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
	}


}
