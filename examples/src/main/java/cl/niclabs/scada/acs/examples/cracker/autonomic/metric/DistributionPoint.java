package cl.niclabs.scada.acs.examples.cracker.autonomic.metric;


import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class DistributionPoint implements Serializable {

    private static final DecimalFormat df = new DecimalFormat("0.000", DecimalFormatSymbols.getInstance(Locale.US));

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

    public String asTestLog() {
        return df.format(x*100) + "," + df.format((y-x)*100) + "," + df.format((1-y)*100);
    }
}
