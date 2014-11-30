package cl.niclabs.scada.acs.examples.cracker.solver.components;

import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.examples.cracker.solver.SolverTask;
import org.objectweb.proactive.core.component.type.annotations.multicast.MethodDispatchMetadata;
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatchMetadata;
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatchMode;

import java.util.List;


public interface MulticastSlave {

    static final String NAME = "multicast-" + Slave.NAME;

    @MethodDispatchMetadata(mode = @ParamDispatchMetadata(mode = ParamDispatchMode.ROUND_ROBIN))
    List<Wrapper<String>> workOn(List<SolverTask> solverTask);
}
