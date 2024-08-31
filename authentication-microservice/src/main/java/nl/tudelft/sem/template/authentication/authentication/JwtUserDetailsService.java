package nl.tudelft.sem.template.authentication.authentication;

import java.util.ArrayList;
import java.util.List;

import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.valueobjects.NetId;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.domain.user.valueobjects.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * User details service responsible for retrieving the user from the DB.
 */
@Service
public class JwtUserDetailsService implements UserDetailsService {

    private final transient UserRepository userRepository;

    @Autowired
    public JwtUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads user information required for authentication from the DB.
     *
     * @param username The username of the user we want to authenticate
     * @return The authentication user information of that user
     * @throws UsernameNotFoundException Username was not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var optionalUser = userRepository.findByNetId(new NetId(username));

        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException("User does not exist");
        }

        var user = optionalUser.get();

        //third argument of User: authorities granted. In this case we pass along the role to signify which are granted.
        ArrayList<UserRole> authoritiesGranted = new ArrayList<>();
        authoritiesGranted.add(user.getUserRole());

        //note that userRole is still wrapped, as the last parameter only takes types implementing GrantedAuthority
        return new User(user.getNetId().toString(), user.getPassword().toString(), authoritiesGranted);
    }

    /**
     * Gets all available stores.
     *
     * @return all stores in the database
     */
    public List<AppUser> getAllStores() {
        List<AppUser> allUsers = userRepository.findAll();
        allUsers.removeIf(user -> !user.getUserRole().getAuthority().equals("store"));
        return allUsers;
    }
}
