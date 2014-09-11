package cl.niclabs.scada.acs.multiactivity.compatibility;

import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.multiactivity.compatibility.AnnotationProcessor;
import org.objectweb.proactive.multiactivity.compatibility.MethodGroup;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * TODO: add + "(" to the method name parsing on AnnotationProcessor. (a "lic()" method will match pub"LIC" int lic())
 * Created by mibanez
 */
public class ACSAnnotationProcessor extends AnnotationProcessor {

    public static final String DEFAULT_GROUP_NAME = "__UnlabeledMethods__";
    public static final String ACS_GROUP_NAME = "__AcsControllers__";

    private Map<String, MethodGroup> acsGroups = new HashMap<>();
    private Map<String, MethodGroup> acsMemberships = new HashMap<>();

    public ACSAnnotationProcessor(Class<?> clazz) {
        super(clazz);
        processUnlabeledMethods(clazz);

        MethodGroup acsControllers = new MethodGroup(ACS_GROUP_NAME, true);
        acsControllers.addCompatibleWith(new HashSet<>(getMethodGroups().values()));
        acsControllers.addCompatibleWith(new HashSet<>(acsGroups.values()));

        acsGroups.put(ACS_GROUP_NAME, acsControllers);
        acsMemberships.put("monitor-controller", acsControllers);
    }

    /**
     * All unlabeled methods (without a group) will be added to a generic groups called "__UnlabeledMethods__".
     * This will be used to allow multi activity compatibility between acs nf interfaces and others methods.
     * @param processedClass class to process
     */
    private void processUnlabeledMethods(Class<?> processedClass) {
        MethodGroup mg = new MethodGroup(DEFAULT_GROUP_NAME, false);
        acsGroups.put(DEFAULT_GROUP_NAME, mg);

        // go through each public method of a class
        for (Method method : processedClass.getMethods()) {

            // if it isn't member of a group, add it to the __UnlabeledMethods__ groups
            if (method.getAnnotation(MemberOf.class) == null) {
                String methodSignature = method.toString();
                acsMemberships.put(methodSignature.substring(methodSignature.indexOf(method.getName() + "(")), mg);
            }
        }
    }

    @Override
    public Map<String, MethodGroup> getMethodGroups() {
        Map<String, MethodGroup> groups = super.getMethodGroups();
        groups.putAll(acsGroups);
        return groups;
    }

    @Override
    public Map<String, MethodGroup> getMethodMemberships() {
        Map<String, MethodGroup> memberships = super.getMethodMemberships();
        memberships.putAll(acsMemberships);
        return memberships;
    }

}
