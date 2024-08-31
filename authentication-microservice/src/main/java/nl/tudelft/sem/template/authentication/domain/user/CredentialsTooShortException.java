package nl.tudelft.sem.template.authentication.domain.user;

import nl.tudelft.sem.template.authentication.domain.user.valueobjects.NetId;

/**
 * An exception to tell if entered credentials are too short.
 */
public class CredentialsTooShortException extends Exception {
    private static final long serialVersionUID = -5227647556678418559L;

    public CredentialsTooShortException(NetId netId) {
        super("The password / netId for: " + netId.toString() + " is too short");
    }
}
