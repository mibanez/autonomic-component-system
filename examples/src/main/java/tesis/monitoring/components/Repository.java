package tesis.monitoring.components;

/**
 * Created by Matias on 18-11-2014.
 */
public interface Repository {

    static final String NAME = "repository";

    void store(byte[] encryptedPassword, String password);

    void fail(byte[] encryptedPassword, String msg);
}
