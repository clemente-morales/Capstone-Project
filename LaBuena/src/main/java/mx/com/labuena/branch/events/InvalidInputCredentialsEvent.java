package mx.com.labuena.branch.events;

import mx.com.labuena.branch.models.Credentials;

/**
 * Created by clerks on 8/7/16.
 */

public class InvalidInputCredentialsEvent {
    private final Credentials credentials;

    public InvalidInputCredentialsEvent(Credentials credentials) {
        this.credentials = credentials;
    }

    public Credentials getCredentials() {
        return credentials;
    }
}
