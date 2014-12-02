package cl.niclabs.scada.acs.examples.cracker.component;

import org.objectweb.fractal.api.control.AttributeController;

public interface BalancerAttributes extends AttributeController {

    // DISTRIBUTION POINT

    void setX(double x);
    double getX();

    void setY(double y);
    double getY();
}
