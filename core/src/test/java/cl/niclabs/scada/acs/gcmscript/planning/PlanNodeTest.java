package cl.niclabs.scada.acs.gcmscript.planning;

import cl.niclabs.scada.acs.component.ACSUtils;
import cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm;
import cl.niclabs.scada.acs.component.controllers.planning.PlanningController;
import cl.niclabs.scada.acs.component.controllers.utils.ValidWrapper;
import cl.niclabs.scada.acs.gcmscript.ACSModel;
import cl.niclabs.scada.acs.gcmscript.controllers.planning.PlanNode;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.fscript.model.NodeKind;
import org.objectweb.fractal.fscript.model.Property;
import org.objectweb.fractal.fscript.types.PrimitiveType;

import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created by mibanez
 */
public class PlanNodeTest {

    private static String planId = "foo";
    private static Component host;
    private static PlanningController planningController;
    private static ACSModel acsModel;

    @BeforeClass
    public static void setUp() {
        planningController = mock(PlanningController.class);
        doReturn(new ValidWrapper<>(true)).when(planningController)
                .doPlanFor(eq(planId), Matchers.any(String.class), Matchers.any(ACSAlarm.class));
        doReturn(new ValidWrapper<>(false)).when(planningController).isEnabled(eq(planId));
        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                doReturn(new ValidWrapper<>(true)).when(planningController).isEnabled(eq(planId));
                return new ValidWrapper<>(true);
            }
        }).when(planningController).setEnabled(eq(planId), eq(true));

        host = mock(Component.class);
        try {
            doReturn(planningController).when(host).getFcInterface(ACSUtils.PLANNING_CONTROLLER);
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
            Assert.fail();
        }

        acsModel = mock(ACSModel.class);
        doReturn(new NodeKind("plan",
                new Property("id", PrimitiveType.STRING, false),
                new Property("enabled", PrimitiveType.BOOLEAN, true))).when(acsModel).getNodeKind("plan");


    }

    @Test(expected = NullPointerException.class)
    public void createWithNullMetricProxy() throws NoSuchInterfaceException {
        new PlanNode(acsModel, null, "fooId");
    }

    @Test(expected = NoSuchElementException.class)
    public void readInvalidProperty() throws NoSuchInterfaceException {
        (new PlanNode(acsModel, host, planId)).getProperty("invalid");
    }

    @Test(expected = NoSuchElementException.class)
    public void writeInvalidProperty() throws NoSuchInterfaceException {
        (new PlanNode(acsModel, host, planId)).setProperty("invalid", null);
    }

    @Test
    public void checkMetricFunctions() throws NoSuchInterfaceException {
        PlanNode node = new PlanNode(acsModel, host, planningController, planId);
        assertEquals("foo", node.getProperty("id"));
        Assert.assertFalse((Boolean) node.getProperty("enabled"));
        node.setProperty("enabled", true);
        Assert.assertTrue((Boolean) node.getProperty("enabled"));
    }

}
