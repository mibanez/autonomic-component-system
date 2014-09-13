package cl.niclabs.scada.acs.multiactivity.compatibility;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.multiactivity.compatibility.MethodGroup;

import java.util.Map;

/**
 * Created by mibanez
 */
public class ACSAnnotationProcessorTest {

    @Test
    public void groupsGeneration() {

        @SuppressWarnings({"UnusedDeclaration", "EmptyMethod"})
        class A {
            public void a() {}
            public void b() {}
            public void c() {}
            private void d() {}
        }

        ACSAnnotationProcessor processor = new ACSAnnotationProcessor(A.class);

        // Groups
        Map<String, MethodGroup> groups = processor.getMethodGroups();
        Assert.assertTrue(groups.containsKey(ACSAnnotationProcessor.DEFAULT_GROUP_NAME));
        MethodGroup defaultGroup = groups.get(ACSAnnotationProcessor.DEFAULT_GROUP_NAME);

        Assert.assertTrue(defaultGroup.name.equals(ACSAnnotationProcessor.DEFAULT_GROUP_NAME));
        Assert.assertTrue(defaultGroup.getCompatibleWith().isEmpty());
        Assert.assertFalse(defaultGroup.isSelfCompatible());

        Assert.assertTrue(groups.containsKey(ACSAnnotationProcessor.ACS_GROUP_NAME));
        MethodGroup acsGroup = groups.get(ACSAnnotationProcessor.ACS_GROUP_NAME);

        Assert.assertTrue(acsGroup.isSelfCompatible());
        Assert.assertTrue(acsGroup.isCompatibleWith(defaultGroup));

        // Methods
        Map<String, MethodGroup> membership = processor.getMethodMemberships();
        Assert.assertTrue(membership.containsKey("a()") && membership.get("a()").equals(defaultGroup));
        Assert.assertTrue(membership.containsKey("b()") && membership.get("b()").equals(defaultGroup));
        Assert.assertTrue(membership.containsKey("c()") && membership.get("c()").equals(defaultGroup));
        Assert.assertFalse(membership.containsKey("d()"));
    }

}
