package cl.niclabs.scada.acs.component.controllers;

import java.io.Serializable;

public interface Wrapper<TYPE extends Serializable> extends Serializable {

    TYPE unwrap() throws CommunicationException;

}
