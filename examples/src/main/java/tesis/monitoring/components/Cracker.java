package tesis.monitoring.components;

public interface Cracker {

    static final String NAME = "cracker";

    void crack(byte[] encryptedPassword);

}
