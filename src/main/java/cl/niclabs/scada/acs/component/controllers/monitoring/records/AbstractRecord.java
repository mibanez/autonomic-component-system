package cl.niclabs.scada.acs.component.controllers.monitoring.records;

import org.objectweb.proactive.core.body.tags.tag.CMTag;
import org.objectweb.proactive.core.jmx.notification.RequestNotificationData;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


@SuppressWarnings("UnusedDeclaration")
public abstract class AbstractRecord {

    // The ? is a "reluctant" quantifier, to make the .* to match the smallest possible string.
    private Pattern pattern = Pattern.compile("\\[TAG\\](.*?)\\[DATA\\](.*?)\\[END\\]");
    private Pattern inner = Pattern.compile("\\[(.*?)\\]");

    private long currentId; // Id of this request
    private long parentId;  // Id of the parent request
    private long rootId;    // Id of the original request that generated this request

    private String source;
    private String destination;
    private String interfaceName;
    private String methodName;

    public AbstractRecord(RequestNotificationData data) throws CMTagNotFoundException {

        boolean cmTagFound = false;
        Matcher match = pattern.matcher(data.getTags());

        while (match.find()) {
            String[] fields = inner.split(match.group());
            if (fields[1].equals(CMTag.IDENTIFIER)) {
                String[] cmTagFields = fields[2].split("::");
                parentId = Long.parseLong(cmTagFields[0]);
                currentId = Long.parseLong(cmTagFields[1]);
                source = cmTagFields[2];
                destination = cmTagFields[3];
                interfaceName = cmTagFields[4];
                methodName = cmTagFields[5];
                rootId = Long.parseLong(cmTagFields[6]);
                cmTagFound = true;
                break;
            }
        }

        if (!cmTagFound) {
            throw new CMTagNotFoundException();
        }
    }

    @Override
    public String toString() {
        return String.format("[current: %d, parent: %d, root: %d, method: %s, source: %s, destination: %s",
                currentId, parentId, rootId, methodName, source, destination);
    }

    public long getCurrentId() {
        return currentId;
    }

    public long getParentId() {
        return parentId;
    }

    public long getRootId() {
        return rootId;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public abstract boolean isFinished();

}
