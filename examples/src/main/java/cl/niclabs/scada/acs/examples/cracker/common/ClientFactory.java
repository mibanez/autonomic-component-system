package cl.niclabs.scada.acs.examples.cracker.common;

import cl.niclabs.scada.acs.component.factory.ACSFactory;
import cl.niclabs.scada.acs.component.factory.ACSFactoryException;
import cl.niclabs.scada.acs.examples.cracker.common.components.Client;
import cl.niclabs.scada.acs.examples.cracker.common.components.ClientImpl;
import cl.niclabs.scada.acs.examples.cracker.common.components.Cracker;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.node.Node;

public class ClientFactory {

    private final ACSFactory factory;

    public ClientFactory(ACSFactory factory) {
        this.factory = factory;
    }

    public Component getClient(String name) throws ACSFactoryException {
        return getClient(name, null);
    }

    public Component getClient(String name, Node node) throws ACSFactoryException {
        ComponentType clientType = getClientType();
        return factory.createPrimitiveComponent(name, clientType, ClientImpl.class, node);
    }

    private ComponentType getClientType() throws ACSFactoryException {
        return factory.createComponentType(new InterfaceType[] {
                factory.createInterfaceType(Client.NAME, Client.class, false, false),
                factory.createInterfaceType(Cracker.NAME, Cracker.class, true, false)
        });
    }
}
