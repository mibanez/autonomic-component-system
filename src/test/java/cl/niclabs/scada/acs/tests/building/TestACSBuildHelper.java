package cl.niclabs.scada.acs.tests.building;

import cl.niclabs.scada.acs.component.utils.ACSBuildHelper;
import cl.niclabs.scada.acs.tests.CommonTest;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.InterfaceType;

/**
 * Created by mibanez on 08-09-14.
 */
public class TestACSBuildHelper extends CommonTest {

    @Test(expected = NullPointerException.class)
    public void nullFactoryOnGetStandardNfInterfaces() {
        try {
            ACSBuildHelper.getStandardNfInterfaces(null);
        } catch (InstantiationException e) {
            Assert.fail("a NullPointerException was expected");
        }
    }

    @Test(expected = NullPointerException.class)
    public void nullFactoryOnGetAcsNfInterfaces() {
        try {
            ACSBuildHelper.getAcsNfInterfaces(null, new InterfaceType[]{});
        } catch (InstantiationException e) {
            Assert.fail("a NullPointerException was expected");
        }
    }

    @Test(expected = NullPointerException.class)
    public void nullInterfacesOnGetAcsNfInterfaces() {
        try {
            ACSBuildHelper.getAcsNfInterfaces(typeFactory, null);
        } catch (InstantiationException e) {
            Assert.fail("a NullPointerException was expected");
        }
    }

}
