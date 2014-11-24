package tesis.monitoring.components;

/**
 * Created by Matias on 23-11-2014.
 */
public interface Client {

    static final String NAME = "client";

    void start(int numberOfTest, int delay);

}
