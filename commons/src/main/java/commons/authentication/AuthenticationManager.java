package commons.authentication;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Iterator;

@Component
public class AuthenticationManager {
    /**
    * Interfaces with spring security to get the name of the user in the current context.
    *
    * @return The name of the user.
    */
    public String getNetId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public String getRole() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().toString();
    }

}
