package tesis.monitoring.metrics;

import cl.niclabs.scada.acs.component.controllers.monitoring.events.RecordEvent;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.Metric;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.MetricStore;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.RecordStore;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import tesis.monitoring.components.Cracker;
import tesis.monitoring.components.Repository;

/**
 * ProgressMetric
 * Created by Matias on 20-11-2014.
 */
public class PendingRequests extends Metric<String> {

    public static final String NAME = "progress";
    private String progress = "none";

    public PendingRequests() {
        subscribeTo(RecordEvent.VOID_REQUEST_SENT);
    }

    @Override
    public String getValue() {
        return progress;
    }

    @Override
    public String calculate(RecordStore recordStore, MetricStore metricStore) {
        Wrapper<Long> repoValue = metricStore.getValue(StoredPasswordCounter.NAME, new String[] {
                Cracker.NAME,
                Repository.NAME
        });
        Wrapper<Long> crackerValue = metricStore.getValue(CrackRequestCounter.NAME, new String[] {
                Cracker.NAME
        });

        long crackedRequests = crackerValue.unwrap();
        long storedPasswords = repoValue.unwrap();

        progress = String.valueOf(crackedRequests).concat("\t").concat(String.valueOf(storedPasswords));
        System.out.println(System.currentTimeMillis() + "\tMON\t" + progress);

        return progress;
    }
}
