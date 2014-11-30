package cl.niclabs.scada.acs.gcmscript.analysis;

import cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm;
import cl.niclabs.scada.acs.component.controllers.analysis.AnalysisController;
import cl.niclabs.scada.acs.component.controllers.utils.ValidWrapper;
import cl.niclabs.scada.acs.gcmscript.model.ACSModel;
import cl.niclabs.scada.acs.gcmscript.model.controllers.analysis.RuleNode;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.fscript.model.NodeKind;
import org.objectweb.fractal.fscript.model.Property;
import org.objectweb.fractal.fscript.types.PrimitiveType;

import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;

/**
 * Created by mibanez
 */
public class RuleNodeTest {

    private static Component host;
    private static AnalysisController analysisController;
    private static String ruleId = "foo";
    private static ACSModel acsModel;

    @BeforeClass
    public static void setUp() {
        analysisController = Mockito.mock(AnalysisController.class);
        Mockito.doReturn(new ValidWrapper<>(false)).when(analysisController).isEnabled(ruleId);
        Mockito.doReturn(new ValidWrapper<>(ACSAlarm.WARNING)).when(analysisController).getAlarm(ruleId);
        Mockito.doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Mockito.doReturn(new ValidWrapper<>(true)).when(analysisController).isEnabled(ruleId);
                return new ValidWrapper<>(true);
            }
        }).when(analysisController).setEnabled(ruleId, true);


        acsModel = Mockito.mock(ACSModel.class, Mockito.RETURNS_MOCKS);
        Mockito.doReturn(new NodeKind("rule",
                new Property("id", PrimitiveType.STRING, false),
                new Property("alarm", PrimitiveType.OBJECT, false),
                new Property("enabled", PrimitiveType.BOOLEAN, true))).when(acsModel).getNodeKind("rule");
    }

    @Test(expected = NullPointerException.class)
    public void createWithNullHostProxy() throws NoSuchInterfaceException {
        new RuleNode(acsModel, null, ruleId);
    }

    @Test(expected = NoSuchElementException.class)
    public void readInvalidProperty() throws NoSuchInterfaceException {
        (new RuleNode(acsModel, host, analysisController, ruleId)).getProperty("invalid");
    }

    @Test(expected = NoSuchElementException.class)
    public void writeInvalidProperty() throws NoSuchInterfaceException {
        (new RuleNode(acsModel, host, analysisController, ruleId)).setProperty("invalid", null);
    }

    @Test
    public void checkMetricFunctions() throws NoSuchInterfaceException {
        RuleNode node = new RuleNode(acsModel, host, analysisController, ruleId);
        assertEquals("foo", node.getProperty("id"));
        assertEquals(ACSAlarm.WARNING, node.getProperty("alarm"));
        Assert.assertFalse((Boolean) node.getProperty("enabled"));
        node.setProperty("enabled", true);
        Assert.assertTrue((Boolean) node.getProperty("enabled"));
    }

}
