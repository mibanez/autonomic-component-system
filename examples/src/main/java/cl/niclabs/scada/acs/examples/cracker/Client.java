package cl.niclabs.scada.acs.examples.cracker;

public interface Client {

    static final String NAME = "client-itf";

    void start(String alphabet, int maxLength, int numberOfTest, long delay);
}
