package cl.niclabs.scada.acs.gcmscript.planning;

import cl.niclabs.scada.acs.component.controllers.ACSAlarm;
import cl.niclabs.scada.acs.component.controllers.CommunicationException;
import cl.niclabs.scada.acs.component.controllers.PlanProxy;
import cl.niclabs.scada.acs.gcmscript.ACSModel;
import cl.niclabs.scada.acs.gcmscript.controllers.planning.PlanNode;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.objectweb.fractal.fscript.model.NodeKind;
import org.objectweb.fractal.fscript.model.Property;
import org.objectweb.fractal.fscript.types.PrimitiveType;

import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;

/**
 * Created by mibanez
 */
public class PlanNodeTest {

    private static PlanProxy planProxy;
    private static ACSModel acsModel;

    @BeforeClass
    public static void setUp() {
        planProxy = Mockito.mock(PlanProxy.class);
        Mockito.doReturn("foo").when(planProxy).getId();
        try {
            Mockito.doNothing().when(planProxy).doPlanFor(Matchers.any(String.class), Matchers.any(ACSAlarm.class));
            Mockito.doReturn(false).when(planProxy).isEnabled();
            Mockito.doAnswer(new Answer() {
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                    Mockito.doReturn(true).when(planProxy).isEnabled();
                    return null;
                }
            }).when(planProxy).setEnabled(true);

        } catch (CommunicationException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        acsModel = Mockito.mock(ACSModel.class);
        Mockito.doReturn(new NodeKind("plan",
                new Property("id", PrimitiveType.STRING, false),
                new Property("enabled", PrimitiveType.BOOLEAN, true))).when(acsModel).getNodeKind("plan");
    }

    @Test(expected = NullPointerException.class)
    public void createWithNullMetricProxy() {
        new PlanNode(acsModel, null);
    }

    @Test(expected = NoSuchElementException.class)
    public void readInvalidProperty() {
        (new PlanNode(acsModel, planProxy)).getProperty("invalid");
    }

    @Test(expected = NoSuchElementException.class)
    public void writeInvalidProperty() {
        (new PlanNode(acsModel, planProxy)).setProperty("invalid", null);
    }

    @Test
    public void checkMetricFunctions() {
        PlanNode node = new PlanNode(acsModel, planProxy);
        assertEquals("foo", node.getProperty("id"));
        Assert.assertFalse((Boolean) node.getProperty("enabled"));
        node.setProperty("enabled", true);
        Assert.assertTrue((Boolean) node.getProperty("enabled"));
    }

}
