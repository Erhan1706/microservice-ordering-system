package nl.tudelft.sem.template.authentication.domain.user;

import nl.tudelft.sem.template.authentication.domain.user.valueobjects.NetId;


public class CredentialsTooLongException extends Exception {

    private static final long serialVersionUID = -5227647556678415529L;

    public CredentialsTooLongException(NetId netId) {
        super("The password / netId for: " + netId.toString() + " is too long");
    }
}
