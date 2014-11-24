package cl.niclabs.scada.acs.examples.optimizer.components;

import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;

/**
 * Created by mibanez
 */
public interface Cracker {

    static final String NAME = "cracker-itf";

    Wrapper<String> crack(byte[] encryptedPassword);

}
