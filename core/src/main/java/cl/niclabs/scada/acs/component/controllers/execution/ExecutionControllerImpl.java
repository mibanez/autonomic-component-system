package cl.niclabs.scada.acs.component.controllers.execution;

import cl.niclabs.scada.acs.component.controllers.utils.ValidWrapper;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.component.controllers.utils.WrongWrapper;
import cl.niclabs.scada.acs.gcmscript.controllers.GenericElementNode;
import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.fscript.*;
import org.objectweb.proactive.core.component.PAInterfaceImpl;
import org.objectweb.proactive.core.component.componentcontroller.AbstractPAComponentController;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.extra.component.fscript.GCMScript;
import org.objectweb.proactive.extra.component.fscript.exceptions.ReconfigurationException;
import org.objectweb.proactive.extra.component.fscript.model.GCMComponentNode;
import org.objectweb.proactive.extra.component.fscript.model.GCMInterfaceNode;
import org.objectweb.proactive.extra.component.fscript.model.GCMNodeFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Serializable;
import java.util.Set;

/**
 * Created by mibanez
 */
public class ExecutionControllerImpl extends AbstractPAComponentController implements ExecutionController {

    public static final String CONTROLLER_NAME = "ExecutionController";
    public static final String EXECUTION_CONTROLLER_SERVER_ITF = "execution-controller-server-itf-nf";
    public static final String ACS_GCMSCRIPT_ADL = "cl.niclabs.scada.acs.gcmscript.ACSScript";

    private transient ScriptLoader loader;
    private transient FScriptEngine engine;


    private void init() throws ReconfigurationException {
        if (loader == null || engine == null) {
            try {
                String defaultFcProvider = System.getProperty("fractal.provider");
                if (defaultFcProvider == null) {
                    defaultFcProvider = System.getProperty("gcm.provider");
                    if (defaultFcProvider == null) {
                        throw new ReconfigurationException("Unable to find component model provider");
                    }
                }

                System.setProperty("fractal.provider", "org.objectweb.fractal.julia.Julia");
                Component gcmScript = GCMScript.newEngineFromAdl(ACS_GCMSCRIPT_ADL);
                loader = FScript.getScriptLoader(gcmScript);
                engine = FScript.getFScriptEngine(gcmScript);
                GCMNodeFactory nodeFactory = (GCMNodeFactory) FScript.getNodeFactory(gcmScript);
                engine.setGlobalVariable("this", nodeFactory.createGCMComponentNode(hostComponent));
                System.setProperty("fractal.provider", defaultFcProvider);

            } catch (Exception e) {
                throw new ReconfigurationException("Unable to set new engine for reconfiguration controller", e);
            }
        }
    }

    @Override
    public Wrapper<String[]> load(String fileName) {
        try {
            init();
            Set<String> result = loader.load(new FileReader(fileName));
            return new ValidWrapper<>(result.toArray(new String[result.size()]));
        } catch (FileNotFoundException | InvalidScriptException e) {
            return new WrongWrapper<>("Unable to load procedure definitions: " + e.getMessage());
        } catch (ReconfigurationException re) {
            return new WrongWrapper<>("ReconfigurationException: " + re.getMessage());
        }
    }

    @Override
    public Wrapper<String[]> getGlobals() {
        try {
            init();
            Set<String> result = engine.getGlobals();
            return new ValidWrapper<>(result.toArray(new String[result.size()]));
        } catch (ReconfigurationException re) {
            return new WrongWrapper<>("ReconfigurationException: " + re.getMessage());
        }
    }

    @Override
    public <REPLY extends Serializable> Wrapper<REPLY> execute(String source) {
        try {
            init();
            Object result = engine.execute(source);
            if (result == null) {
                return new ValidWrapper<>(null);
            }
            return new ValidWrapper<>((REPLY) getRepresentation(result));
        } catch (ReconfigurationException e) {
            return new WrongWrapper<>("ReconfigurationException: " + e.getMessage());
        } catch (FScriptException e) {
            return new WrongWrapper<>("FScriptException: " + e.getMessage());
        }
    }

    private Serializable getRepresentation(Object object) {

        if (object instanceof GCMComponentNode) {
            Component comp = ((GCMComponentNode) object).getComponent();
            if (comp instanceof PAComponent) {
                return (PAComponent) comp;
            }
            try {
                return String.format("[Not Serializable Component: %s]", GCM.getNameController(comp));
            } catch (NoSuchInterfaceException e) {
                return "[Not Serializable Unknown Component]";
            }
        }

        if (object instanceof GCMInterfaceNode) {
            Interface itf = ((GCMInterfaceNode) object).getInterface();
            if (itf instanceof PAInterfaceImpl) {
                return (PAInterfaceImpl) itf;
            }
            return String.format("[Not Serializable Interface: %s]", itf.getFcItfName());
        }

        if (object instanceof GenericElementNode) {
            return ((GenericElementNode) object).getElementProxy();
        }

        return object.toString();
    }
}
