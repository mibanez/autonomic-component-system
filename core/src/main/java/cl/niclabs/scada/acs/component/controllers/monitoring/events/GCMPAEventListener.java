package cl.niclabs.scada.acs.component.controllers.monitoring.events;


import org.objectweb.proactive.core.UniqueID;

public interface GCMPAEventListener {

    static final String ITF_NAME = "gcmpa-event-listener-nf";

    void init(UniqueID uniqueId, String runtimeURL);
    void stop();
}
