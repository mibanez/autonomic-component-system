package cl.niclabs.scada.acs.component.controllers.utils;

import java.io.Serializable;

public interface Wrapper<TYPE extends Serializable> extends Serializable {

    /** Returns the wrapped value if this is a valid wrapper, returns null otherwise **/
    TYPE unwrap();

    /** A valid wrapper ensures the wrapped value was obtained correctly, without exceptions  **/
    boolean isValid();

    /** An invalid wrapper usually comes with a error message obtainable with this method **/
    String getMessage();
}
