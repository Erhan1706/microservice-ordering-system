package nl.tudelft.sem.template.authentication.domain.user;

/**
 * An exception to tell if the entered password does not contain a special character.
 */
public class PasswordNotContainSpecial
        extends Exception {
    private static final long serialVersionUID = -3945375186414022589L;

    public PasswordNotContainSpecial(String s) {
        super("The password doesn't contain a special character");
    }
}
