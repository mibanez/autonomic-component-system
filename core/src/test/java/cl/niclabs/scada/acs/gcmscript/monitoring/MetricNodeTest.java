package cl.niclabs.scada.acs.gcmscript.monitoring;

import cl.niclabs.scada.acs.component.controllers.CommunicationException;
import cl.niclabs.scada.acs.component.controllers.MetricProxy;
import cl.niclabs.scada.acs.gcmscript.ACSModel;
import cl.niclabs.scada.acs.gcmscript.controllers.monitoring.MetricNode;
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
public class MetricNodeTest  {

    private static MetricProxy metricProxy;
    private static ACSModel acsModel;

    @BeforeClass
    public static void setUp() {
        metricProxy = Mockito.mock(MetricProxy.class);
        Mockito.doReturn("foo").when(metricProxy).getId();
        try {
            Mockito.doReturn("value").when(metricProxy).getValue();
            Mockito.doReturn(false).when(metricProxy).isEnabled();
            Mockito.doAnswer(new Answer() {
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                    Mockito.doReturn(true).when(metricProxy).isEnabled();
                    return null;
                }
            }).when(metricProxy).setEnabled(true);

        } catch (CommunicationException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        acsModel = Mockito.mock(ACSModel.class, Mockito.RETURNS_MOCKS);
        Mockito.doReturn(new NodeKind("metric",
                new Property("id", PrimitiveType.STRING, false),
                new Property("value", PrimitiveType.OBJECT, false),
                new Property("enabled", PrimitiveType.BOOLEAN, true))).when(acsModel).getNodeKind("metric");
    }

    @Test(expected = NullPointerException.class)
    public void createWithNullMetricProxy() {
        new MetricNode(acsModel, null);
    }

    @Test(expected = NoSuchElementException.class)
    public void readInvalidProperty() {
        (new MetricNode(acsModel, metricProxy)).getProperty("invalid");
    }

    @Test(expected = NoSuchElementException.class)
    public void writeInvalidProperty() {
        (new MetricNode(acsModel, metricProxy)).setProperty("invalid", null);
    }

    @Test
    public void checkMetricFunctions() {
        MetricNode node = new MetricNode(acsModel, metricProxy);
        assertEquals("foo", node.getProperty("id"));
        assertEquals("value", node.getProperty("value"));
        Assert.assertFalse((Boolean) node.getProperty("enabled"));
        node.setProperty("enabled", true);
        Assert.assertTrue((Boolean) node.getProperty("enabled"));
    }

}
