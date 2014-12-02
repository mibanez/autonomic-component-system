package cl.niclabs.scada.acs.examples.cracker.autonomic;


import java.io.Serializable;

public class DistributionPoint implements Serializable {

    private double x;
    private double y;

    public DistributionPoint() {
        this.x = 1.0/3.0;
        this.y = 2*x;
    }

    public DistributionPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        return String.format("[%.3f, %.3f, %.3f]", x*100, (y-x)*100, (1-y)*100);
    }
}
