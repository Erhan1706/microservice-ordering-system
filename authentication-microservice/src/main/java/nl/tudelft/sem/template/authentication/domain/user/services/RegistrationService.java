package nl.tudelft.sem.template.authentication.domain.user.services;

import nl.tudelft.sem.template.authentication.domain.user.*;
import nl.tudelft.sem.template.authentication.domain.user.valueobjects.HashedPassword;
import nl.tudelft.sem.template.authentication.domain.user.valueobjects.NetId;
import nl.tudelft.sem.template.authentication.domain.user.valueobjects.Password;
import nl.tudelft.sem.template.authentication.domain.user.valueobjects.UserRole;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * A DDD service for registering a new user.
 */
@SuppressWarnings("PMD")
@Service
public class RegistrationService {
    private final transient UserRepository userRepository;
    private final transient PasswordHashingService passwordHashingService;

    /**
     * Instantiates a new UserService.
     *
     * @param userRepository  the user repository
     * @param passwordHashingService the password encoder
     */
    public RegistrationService(UserRepository userRepository, PasswordHashingService passwordHashingService) {
        this.userRepository = userRepository;
        this.passwordHashingService = passwordHashingService;
    }

    /**
     * Register a new user.
     *
     * @param netId    The NetID of the user
     * @param password The password of the user
     * @throws Exception if the user already exists or credentials are invalid
     */
    public AppUser registerUser(NetId netId, Password password, UserRole userRole) throws Exception {
        if (checkNetIdIsUnique(netId)) {
            throw new NetIdAlreadyInUseException(netId);
        }
        ValidationUtils.validate(netId, password);

        HashedPassword hashedPassword = passwordHashingService.hash(password);
        List<Long> allergies = new ArrayList<>();
        allergies.add(0L);

        AppUser user = new AppUser(netId, hashedPassword, userRole, allergies);
        userRepository.save(user);
        return user;
    }

    public boolean checkNetIdIsUnique(NetId netId) {
        return userRepository.existsByNetId(netId);
    }
}
