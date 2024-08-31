package nl.tudelft.sem.template.authentication.domain.user.services;

import nl.tudelft.sem.template.authentication.domain.user.CredentialsTooLongException;
import nl.tudelft.sem.template.authentication.domain.user.CredentialsTooShortException;
import nl.tudelft.sem.template.authentication.domain.user.PasswordNotContainNumber;
import nl.tudelft.sem.template.authentication.domain.user.PasswordNotContainSpecial;
import nl.tudelft.sem.template.authentication.domain.user.valueobjects.NetId;
import nl.tudelft.sem.template.authentication.domain.user.valueobjects.Password;

public class ValidationUtils {

    public static boolean validate(NetId netId, Password password) throws Exception {
        if (!passwordContainsNumber(password)) {
            throw new PasswordNotContainNumber(password.toString());
        }
        if (!containsSpecial(password)) {
            throw new PasswordNotContainSpecial(password.toString());
        }
        checkCredentials(netId, password);
        return true;
    }

    private static boolean checkCredentials(NetId netId, Password password) throws Exception {
        if (netId.toString().length() < 7 || password.toString().length() < 7) {
            throw new CredentialsTooShortException(netId);
        }
        if (netId.toString().length() > 20 || password.toString().length() > 20) {
            throw new CredentialsTooLongException(netId);
        }
        return true;
    }

    private static boolean passwordContainsNumber(Password password) {
        String pw = password.toString();
        for (int i = 0; i < pw.length(); i++) {
            if (Character.isDigit(pw.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsSpecial(Password password) {
        String pw = password.toString();
        for (int i = 0; i < pw.length(); i++) {
            if ("!@#$%&*()'+,-./:;<=>?[]^_`{|}".contains(Character.toString(pw.charAt(i)))) {
                return true;
            }
        }
        return false;
    }
}
