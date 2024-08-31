package nl.tudelft.sem.template.authentication.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import nl.tudelft.sem.template.authentication.domain.user.services.PasswordHashingService;
import nl.tudelft.sem.template.authentication.domain.user.services.RegistrationService;
import nl.tudelft.sem.template.authentication.domain.user.valueobjects.HashedPassword;
import nl.tudelft.sem.template.authentication.domain.user.valueobjects.NetId;
import nl.tudelft.sem.template.authentication.domain.user.valueobjects.Password;
import nl.tudelft.sem.template.authentication.domain.user.valueobjects.UserRole;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
// activate profiles to have spring use mocks during auto-injection of certain beans.
@ActiveProfiles({"test", "mockPasswordEncoder"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class RegistrationServiceTests {

    @Autowired
    private transient RegistrationService registrationService;

    @Autowired
    private transient PasswordHashingService mockPasswordEncoder;

    @Autowired
    private transient UserRepository userRepository;

    @Test
    public void createUser_withValidData_worksCorrectly() throws Exception {
        // Arrange
        final NetId testUser = new NetId("SomeUser");
        final Password testPassword = new Password("password123!");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        final UserRole testUserRole = new UserRole("customer");
        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);

        // Act
        registrationService.registerUser(testUser, testPassword, testUserRole);

        // Assert
        AppUser savedUser = userRepository.findByNetId(testUser).orElseThrow();

        assertThat(savedUser.getNetId()).isEqualTo(testUser);
        assertThat(savedUser.getPassword()).isEqualTo(testHashedPassword);
        assertThat(savedUser.getUserRole()).isEqualTo(testUserRole);
    }

    @Test
    public void createUser_withExistingUser_throwsException() {
        // Arrange
        final NetId testUser = new NetId("SomeUser");
        final HashedPassword existingTestPassword = new HashedPassword("password123");
        final Password newTestPassword = new Password("password456!");
        final UserRole testUserRole = new UserRole("customer");
        final List<Long> allergies = new ArrayList<>();
        allergies.add(0L);

        AppUser existingAppUser = new AppUser(testUser, existingTestPassword, testUserRole, allergies);
        userRepository.save(existingAppUser);

        // Act
        ThrowingCallable action = () -> registrationService.registerUser(testUser, newTestPassword, testUserRole);

        // Assert
        assertThatExceptionOfType(Exception.class)
                .isThrownBy(action);

        AppUser savedUser = userRepository.findByNetId(testUser).orElseThrow();

        assertThat(savedUser.getNetId()).isEqualTo(testUser);
        assertThat(savedUser.getPassword()).isEqualTo(existingTestPassword);

        assertThat(savedUser.getUserRole()).isEqualTo(testUserRole);
    }

    @Test
    public void password_no_number_throwsException() {
        // Arrange
        final NetId testUser = new NetId("SomeUser");
        final HashedPassword hashedPassword = new HashedPassword("password@@");
        final Password newTestPassword = new Password("password!");
        final UserRole testUserRole = new UserRole("customer");
        final List<Long> allergies = new ArrayList<>();
        allergies.add(0L);
        when(mockPasswordEncoder.hash(newTestPassword)).thenReturn(hashedPassword);


        // Act
        ThrowingCallable action = () -> registrationService.registerUser(testUser, newTestPassword, testUserRole);

        // Assert
        assertThatExceptionOfType(PasswordNotContainNumber.class)
            .isThrownBy(action);
    }

    @Test
    public void credentials_too_long_throwsException() {
        // Arrange
        final NetId testUser = new NetId("SomeUser");
        final HashedPassword hashedPassword = new HashedPassword("passwordtestestestestestestes@@");
        final Password newTestPassword = new Password("password!testestest9estestes");
        final UserRole testUserRole = new UserRole("customer");
        final List<Long> allergies = new ArrayList<>();
        allergies.add(0L);
        when(mockPasswordEncoder.hash(newTestPassword)).thenReturn(hashedPassword);


        // Act
        ThrowingCallable action = () -> registrationService.registerUser(testUser, newTestPassword, testUserRole);

        // Assert
        assertThatExceptionOfType(CredentialsTooLongException.class)
            .isThrownBy(action);
    }

    @Test
    public void password_no_special_throwsException() {
        // Arrange
        final NetId testUser = new NetId("SomeUser");
        final HashedPassword hashedPassword = new HashedPassword("password12");
        final Password newTestPassword = new Password("password12");
        final UserRole testUserRole = new UserRole("customer");
        final List<Long> allergies = new ArrayList<>();
        allergies.add(0L);
        when(mockPasswordEncoder.hash(newTestPassword)).thenReturn(hashedPassword);


        // Act
        ThrowingCallable action = () -> registrationService.registerUser(testUser, newTestPassword, testUserRole);

        // Assert
        assertThatExceptionOfType(PasswordNotContainSpecial.class)
            .isThrownBy(action);
    }

    @Test
    public void credentials_too_short_throwsException() {
        // Arrange
        final NetId testUser = new NetId("Some");
        final HashedPassword hashedPassword = new HashedPassword("password12@@");
        final Password newTestPassword = new Password("password12!");
        final UserRole testUserRole = new UserRole("customer");
        final List<Long> allergies = new ArrayList<>();
        allergies.add(0L);
        when(mockPasswordEncoder.hash(newTestPassword)).thenReturn(hashedPassword);


        // Act
        ThrowingCallable action = () -> registrationService.registerUser(testUser, newTestPassword, testUserRole);

        // Assert
        assertThatExceptionOfType(CredentialsTooShortException.class)
            .isThrownBy(action);
    }
}
