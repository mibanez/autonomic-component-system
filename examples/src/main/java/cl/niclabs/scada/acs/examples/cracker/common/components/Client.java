package cl.niclabs.scada.acs.examples.cracker.common.components;

public interface Client {

    static final String NAME = "client-itf";

    void start(int maxLength, int numberOfTest, long delay);
}
