package cl.niclabs.scada.acs.examples.optimizer.components;

import cl.niclabs.scada.acs.component.controllers.Wrapper;

/**
 * Created by mibanez
 */
public interface Cracker {

    Wrapper<String> crack(byte[] encryptedPassword);

}
