package cl.niclabs.scada.acs.gcmscript.analysis;

import cl.niclabs.scada.acs.component.controllers.CommunicationException;
import cl.niclabs.scada.acs.component.controllers.RuleProxy;
import cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm;
import cl.niclabs.scada.acs.gcmscript.ACSModel;
import cl.niclabs.scada.acs.gcmscript.controllers.analysis.RuleNode;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
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
public class RuleNodeTest {

    private static RuleProxy ruleProxy;
    private static ACSModel acsModel;

    @BeforeClass
    public static void setUp() {
        ruleProxy = Mockito.mock(RuleProxy.class);
        Mockito.doReturn("foo").when(ruleProxy).getId();
        try {
            Mockito.doReturn(false).when(ruleProxy).isEnabled();
            Mockito.doReturn(ACSAlarm.WARNING).when(ruleProxy).getAlarm();
            Mockito.doAnswer(new Answer() {
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                    Mockito.doReturn(true).when(ruleProxy).isEnabled();
                    return null;
                }
            }).when(ruleProxy).setEnabled(true);

        } catch (CommunicationException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        acsModel = Mockito.mock(ACSModel.class, Mockito.RETURNS_MOCKS);
        Mockito.doReturn(new NodeKind("rule",
                new Property("id", PrimitiveType.STRING, false),
                new Property("alarm", PrimitiveType.OBJECT, false),
                new Property("enabled", PrimitiveType.BOOLEAN, true))).when(acsModel).getNodeKind("rule");
    }

    @Test(expected = NullPointerException.class)
    public void createWithNullMetricProxy() {
        new RuleNode(acsModel, null);
    }

    @Test(expected = NoSuchElementException.class)
    public void readInvalidProperty() {
        (new RuleNode(acsModel, ruleProxy)).getProperty("invalid");
    }

    @Test(expected = NoSuchElementException.class)
    public void writeInvalidProperty() {
        (new RuleNode(acsModel, ruleProxy)).setProperty("invalid", null);
    }

    @Test
    public void checkMetricFunctions() {
        RuleNode node = new RuleNode(acsModel, ruleProxy);
        assertEquals("foo", node.getProperty("id"));
        assertEquals(ACSAlarm.WARNING, node.getProperty("alarm"));
        Assert.assertFalse((Boolean) node.getProperty("enabled"));
        node.setProperty("enabled", true);
        Assert.assertTrue((Boolean) node.getProperty("enabled"));
    }

}
