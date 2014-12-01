package tesis.monitoring.components;

public interface Cracker {

    static final String NAME = "cracker-itf";

    void crack(byte[] encryptedPassword);

}
