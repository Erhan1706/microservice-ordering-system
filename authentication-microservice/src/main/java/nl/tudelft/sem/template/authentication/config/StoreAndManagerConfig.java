package nl.tudelft.sem.template.authentication.config;

import commons.Ingredient;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.domain.user.services.PasswordHashingService;
import nl.tudelft.sem.template.authentication.domain.user.valueobjects.HashedPassword;
import nl.tudelft.sem.template.authentication.domain.user.valueobjects.NetId;
import nl.tudelft.sem.template.authentication.domain.user.valueobjects.Password;
import nl.tudelft.sem.template.authentication.domain.user.valueobjects.UserRole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.List;

@Profile("!test")
@Configuration
public class StoreAndManagerConfig {

    /**
     * This method populates the user database with some default stores and 1 default manager upon starting.
     */
    @Bean
    CommandLineRunner commandLineRunnerUserRepo(UserRepository userRepo, PasswordHashingService pwHashingService) {
        return args -> {
            userRepo.flush();

            UserRole storeUserRole = new UserRole("store");
            UserRole managerUserRole = new UserRole("manager");
            List<Long> allergies = new ArrayList<>(List.of(0L));

            NetId netIdStore1 = new NetId("Delft");
            Password passwordStore1 = new Password("hello");
            HashedPassword hashedPasswordStore1 = pwHashingService.hash(passwordStore1);
            AppUser store1 = new AppUser(netIdStore1, hashedPasswordStore1, storeUserRole, allergies);
            userRepo.save(store1);

            NetId netIdStore2 = new NetId("Rotterdam");
            Password passwordStore2 = new Password("goodbye");
            HashedPassword hashedPasswordStore2 = pwHashingService.hash(passwordStore2);
            AppUser store2 = new AppUser(netIdStore2, hashedPasswordStore2, storeUserRole, allergies);
            userRepo.save(store2);

            NetId netIdDefaultManager = new NetId("The Manager");
            Password passwordManager = new Password("unlimited power");
            HashedPassword hashedPasswordManager = pwHashingService.hash(passwordManager);
            AppUser manager = new AppUser(netIdDefaultManager, hashedPasswordManager, managerUserRole, allergies);
            userRepo.save(manager);
        };
    }
}
