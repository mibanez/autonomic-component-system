package cl.niclabs.scada.acs.component.adl.implementations;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.components.Component;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.task.core.TaskMap;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.adl.implementations.PAImplementationCompiler;
import org.objectweb.proactive.core.component.body.ComponentRunActive;
import org.objectweb.proactive.core.component.type.Composite;
import org.objectweb.proactive.multiactivity.component.ComponentMultiActiveService;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


/**
 * A copy of PAImplementationCompiler, this version overrides a private method.
 *
 */
public class ACSImplementationCompiler extends PAImplementationCompiler implements Serializable {

    private static final String ACS_COMPONENT_CONFIG_FILE_LOCATION =
            "/cl/niclabs/scada/acs/component/default-acs-component-config.xml";

    protected interface ACSActive extends ComponentRunActive, Serializable { }

    private ACSActive acsActive = new ACSActive() {
        @Override
        public void runComponentActivity(Body body) {
            body.setImmediateService("getValue", false);
            (new ComponentMultiActiveService(body)).multiActiveServing();
        }
    };

    @Override
    public void compile(List<ComponentContainer> path, ComponentContainer container, TaskMap tasks,
            Map<Object, Object> context) throws ADLException {

        //DEBUG
        String name = null;
        if (container instanceof Definition) {
            name = ((Definition) container).getName();
        } else if (container instanceof Component) {
            name = ((Component) container).getName();
        }
        boolean f = (container.astGetDecoration("NF") == null);
        PAImplementationCompiler.logger.debug("[PAImplementationCompiler] Compiling " + (f ? "F" : "NF") + " component: " + name);
        //--DEBUG

        // collect info required for creating the component
        ObjectsContainer obj = init(path, container, tasks, context);
        // determines content description and controller description info
        setControllers(obj.getImplementation(), obj.getController(), obj.getName(), obj);
        // create the task that will be in charge of creating the component

        end(tasks, container, context, obj.getName(), obj.getDefinition(), obj.getControllerDesc(),
                obj.getContentDesc(), obj.getVn(), obj.isFunctional());
    }


    /** 
     * Completes the collected ObjectsContainer with the ContentDescription and the
     * ControllerDescription objects.<br/><br/>
     * 
     * Determines the hierarchical type (composite/primitive) by checking if the component has 
     * subcomponents or not. In fact, a composite may have an implementation class, in case that
     * the composite must provide an AttributeController interface (so, checking that implementation==null
     * is not enough).<br/><br/>
     * 
     * @param implementation the implementor class
     * @param controller the controller definition (composite, primitive or path to desc file)
     * @param name the name of the component
     * @param obj the ObjectContainers to update
     */
    void setControllers(String implementation, String controller, String name, ObjectsContainer obj) {
        ContentDescription contentDesc = null;
        ControllerDescription controllerDesc;

        if (implementation == null) {
            // a composite component without attributes
            if ("composite".equals(controller) || (controller == null)) {
                controllerDesc = new ControllerDescription(name, Constants.COMPOSITE,
                		PAImplementationCompiler.getControllerPath(ACS_COMPONENT_CONFIG_FILE_LOCATION, name));
                contentDesc = new ContentDescription(Composite.class.getName(), null, acsActive, null);
            } else {
                controllerDesc = new ControllerDescription(name, Constants.COMPOSITE,
                        PAImplementationCompiler.getControllerPath(controller, name));
                // contentDesc ???
            }

        } else if (obj.hasSubcomponents()) {
            // a composite component with attributes 
            //    in that case it must have an Attributes node, and the class implementation must implement
            //    the Attributes signature
            contentDesc = new ContentDescription(implementation, null, acsActive, null);

            // treat it as a composite
            if ("composite".equals(controller) || (controller == null)) {
                controllerDesc = new ControllerDescription(name, Constants.COMPOSITE,
                		PAImplementationCompiler.getControllerPath(ACS_COMPONENT_CONFIG_FILE_LOCATION, name));
            } else {
                controllerDesc = new ControllerDescription(name, Constants.COMPOSITE,
                		PAImplementationCompiler.getControllerPath(controller, name));
            }

        } else {
            // a primitive component
            contentDesc = new ContentDescription(implementation, null, acsActive, null);

            if ("primitive".equals(controller) || (controller == null)) {
                controllerDesc = new ControllerDescription(name, Constants.PRIMITIVE,
                		PAImplementationCompiler.getControllerPath(ACS_COMPONENT_CONFIG_FILE_LOCATION, name));
            } else {
                controllerDesc = new ControllerDescription(name, Constants.PRIMITIVE,
                		PAImplementationCompiler.getControllerPath(controller, name));
            }
        }

        // update the ObjectsContainer object
        obj.setContentDesc(contentDesc);
        obj.setControllerDesc(controllerDesc);
    }
}
