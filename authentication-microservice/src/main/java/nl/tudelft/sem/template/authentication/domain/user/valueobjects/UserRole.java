package nl.tudelft.sem.template.authentication.domain.user.valueobjects;

import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;

/**
 * DDD value object representing a user role in our domain.
 */
@SuppressWarnings("PMD")
@EqualsAndHashCode
public class UserRole implements GrantedAuthority {

    private final transient String userRole;

    //maybe make UserRole take an Enum to be more secure?
    public UserRole(String userRole) {
        this.userRole = userRole;
    }

    @Override
    public String getAuthority() {
        return userRole;
    }

    @Override
    public String toString() {
        return userRole;
    }
}
