package cl.niclabs.scada.acs.component.factory;

import com.sun.istack.internal.NotNull;
import org.objectweb.proactive.core.body.ProActiveMetaObjectFactory;
import org.objectweb.proactive.core.body.request.RequestReceiver;
import org.objectweb.proactive.core.body.request.RequestReceiverFactory;

import java.util.Map;

public class ACSMetaObjectFactory extends ProActiveMetaObjectFactory {

    ACSMetaObjectFactory(@NotNull Map<String, Object> params) {
        super(params);
    }

    protected class ACSRequestReceiverFactory implements RequestReceiverFactory, java.io.Serializable {
        public RequestReceiver newRequestReceiver() {
            return new ACSRequestReceiver();
        }
    }

    @Override
    protected RequestReceiverFactory newRequestReceiverFactorySingleton() {
        return new ACSRequestReceiverFactory();
    }
}
