package cl.niclabs.scada.acs.component.controllers.monitoring.records;

/**
 * Created by Matias on 09-11-2014.
 */
public interface RecordStore {

    RQuery fromAll();
    IncomingRQuery fromIncoming();
    OutgoingRQuery fromOutgoing();
    OutgoingVoidRQuery fromOutgoingVoid();

}
