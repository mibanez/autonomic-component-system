package cl.niclabs.scada.acs.gcmscript.monitoring;

import cl.niclabs.scada.acs.component.ACSManager;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.utils.ValidWrapper;
import cl.niclabs.scada.acs.gcmscript.model.ACSModel;
import cl.niclabs.scada.acs.gcmscript.model.MetricNode;
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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Created by mibanez
 */
public class MetricNodeTest  {

    private static Component host;
    private static MonitoringController monitoringController;
    private static String metricId = "foo";
    private static ACSModel acsModel;

    @BeforeClass
    public static void setUp() throws NoSuchInterfaceException {
        monitoringController = mock(MonitoringController.class);
        Mockito.doReturn(new ValidWrapper<>("value")).when(monitoringController).getValue(metricId);
        Mockito.doReturn(new ValidWrapper<>(false)).when(monitoringController).isEnabled(metricId);
        Mockito.doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Mockito.doReturn(new ValidWrapper<>(true)).when(monitoringController).isEnabled(metricId);
                return new ValidWrapper<>(true);
            }
        }).when(monitoringController).setEnabled(metricId, true);

        host = mock(Component.class);
        doReturn(monitoringController).when(host).getFcInterface(ACSManager.MONITORING_CONTROLLER);

        acsModel = mock(ACSModel.class, Mockito.RETURNS_MOCKS);
        Mockito.doReturn(new NodeKind("metric",
                new Property("id", PrimitiveType.STRING, false),
                new Property("value", PrimitiveType.OBJECT, false),
                new Property("enabled", PrimitiveType.BOOLEAN, true))).when(acsModel).getNodeKind("metric");
    }

    @Test(expected = NullPointerException.class)
    public void createWithNullMetricProxy() throws NoSuchInterfaceException {
        new MetricNode(acsModel, null, metricId);
    }

    @Test(expected = NoSuchElementException.class)
    public void readInvalidProperty() throws NoSuchInterfaceException {
        (new MetricNode(acsModel, host, metricId)).getProperty("invalid");
    }

    @Test(expected = NoSuchElementException.class)
    public void writeInvalidProperty() throws NoSuchInterfaceException {
        (new MetricNode(acsModel, host, metricId)).setProperty("invalid", null);
    }

    @Test
    public void checkMetricFunctions() throws NoSuchInterfaceException {
        MetricNode node = new MetricNode(acsModel, host, monitoringController, metricId);
        assertEquals("foo", node.getProperty("id"));
        assertEquals("value", node.getProperty("value"));
        Assert.assertFalse((Boolean) node.getProperty("enabled"));
        node.setProperty("enabled", true);
        Assert.assertTrue((Boolean) node.getProperty("enabled"));
    }

}
