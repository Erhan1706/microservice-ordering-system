package nl.tudelft.sem.template.authentication.domain.user;

/**
 * An exception to tell if the entered password does not contain a number.
 */
public class PasswordNotContainNumber
        extends Exception {
    private static final long serialVersionUID = -5639218059576329310L;

    public PasswordNotContainNumber(String s) {
        super("The password doesn't contain a number");
    }
}
