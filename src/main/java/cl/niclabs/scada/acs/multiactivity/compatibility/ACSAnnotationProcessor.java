package cl.niclabs.scada.acs.multiactivity.compatibility;

import cl.niclabs.scada.acs.component.controllers.monitoring.MonitorControllerImpl;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.multiactivity.compatibility.AnnotationProcessor;
import org.objectweb.proactive.multiactivity.compatibility.MethodGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: add + "(" to the method name parsing on AnnotationProcessor. (a "lic()" method will match pub"LIC" int lic())
 * Created by mibanez
 */
public class ACSAnnotationProcessor extends AnnotationProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ACSAnnotationProcessor.class);

    public static final String DEFAULT_GROUP_NAME = "__UnlabeledMethods__";
    public static final String ACS_GROUP_NAME = "__AcsControllers__";

    private final Map<String, MethodGroup> acsGroups = new HashMap<>();
    private final Map<String, MethodGroup> acsMemberships = new HashMap<>();

    // indicate if the groups/memberships was already mixed with the user defined groups/membership
    private boolean groupsMixed = false;
    private boolean membershipsMixed = false;


    public ACSAnnotationProcessor(Class<?> clazz) {
        super(clazz);

        MethodGroup defaultGroup = new MethodGroup(DEFAULT_GROUP_NAME, false);
        MethodGroup acsGroup = new MethodGroup(ACS_GROUP_NAME, false);

        defaultGroup.addCompatibleWith(acsGroup);
        acsGroup.addCompatibleWith(defaultGroup);

        acsGroups.put(DEFAULT_GROUP_NAME, defaultGroup);
        acsGroups.put(ACS_GROUP_NAME, acsGroup);

        // fill default group
        processUnlabeledMethods(clazz);

        // fill acs group
        processAcsMethods();
    }

    private void processAcsMethods() {
        for (Method method : MonitorControllerImpl.class.getDeclaredMethods()) {
            String methodName = method.toString().substring(method.toString().indexOf(method.getName() + "("));
            logger.debug("adding [MonitorController.]{} method to group {}", methodName, ACS_GROUP_NAME);
            acsMemberships.put(methodName, acsGroups.get(ACS_GROUP_NAME));
        }
    }

    /**
     * All unlabeled methods (without a group) will be added to a generic groups called "__UnlabeledMethods__".
     * This will be used to allow multi activity compatibility between acs nf interfaces and others methods.
     * @param processedClass class to process
     */
    private void processUnlabeledMethods(Class<?> processedClass) {

        // go through each public method of a class
        for (Method method : processedClass.getMethods()) {

            // if it isn't member of a group, add it to the __UnlabeledMethods__ groups
            if (method.getAnnotation(MemberOf.class) == null) {
                String methodName = method.toString().substring(method.toString().indexOf(method.getName() + "("));
                acsMemberships.put(methodName, acsGroups.get(DEFAULT_GROUP_NAME));
                logger.debug("Adding {} interface to {} group.", methodName, DEFAULT_GROUP_NAME);
            }
        }
    }

    @Override
    public Map<String, MethodGroup> getMethodGroups() {

        if (!groupsMixed) {
            Map<String, MethodGroup> groups = super.getMethodGroups();
            MethodGroup acsGroup = acsGroups.get(ACS_GROUP_NAME);

            // add compatibility to acs group with the user defined groups
            for (MethodGroup mg : groups.values()) {
                acsGroup.addCompatibleWith(mg);
                mg.addCompatibleWith(acsGroup);
            }

            acsGroups.putAll(groups);
            groupsMixed = true;
        }

        return acsGroups;
    }

    @Override
    public Map<String, MethodGroup> getMethodMemberships() {

        if (!membershipsMixed) {
            acsMemberships.putAll(super.getMethodMemberships());
            membershipsMixed = true;
        }

        return acsMemberships;
    }

}
