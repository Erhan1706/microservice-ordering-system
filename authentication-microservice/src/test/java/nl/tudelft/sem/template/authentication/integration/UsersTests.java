package nl.tudelft.sem.template.authentication.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import nl.tudelft.sem.template.authentication.authentication.JwtTokenGenerator;
import nl.tudelft.sem.template.authentication.authentication.JwtUserDetailsService;
import nl.tudelft.sem.template.authentication.controllers.AuthenticationController;
import nl.tudelft.sem.template.authentication.domain.user.*;
import nl.tudelft.sem.template.authentication.domain.user.services.PasswordHashingService;
import nl.tudelft.sem.template.authentication.domain.user.valueobjects.HashedPassword;
import nl.tudelft.sem.template.authentication.domain.user.valueobjects.NetId;
import nl.tudelft.sem.template.authentication.domain.user.valueobjects.Password;
import nl.tudelft.sem.template.authentication.domain.user.valueobjects.UserRole;
import nl.tudelft.sem.template.authentication.integration.utils.JsonUtil;
import nl.tudelft.sem.template.authentication.models.AuthenticationRequestModel;
import nl.tudelft.sem.template.authentication.models.AuthenticationResponseModel;
import nl.tudelft.sem.template.authentication.models.RegistrationRequestModel;
import nl.tudelft.sem.template.authentication.models.RegistrationSpecialRequestModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@ExtendWith(SpringExtension.class)
// activate profiles to have spring use mocks during auto-injection of certain beans.
@ActiveProfiles({"test", "mockPasswordEncoder", "mockTokenGenerator", "mockAuthenticationManager"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class UsersTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private transient PasswordHashingService mockPasswordEncoder;

    @Autowired
    private transient JwtTokenGenerator mockJwtTokenGenerator;

    @Autowired
    private transient AuthenticationManager mockAuthenticationManager;

    @Autowired
    private transient UserRepository userRepository;

    @MockBean
    private transient JwtUserDetailsService mockJwtUserDetailsService;

    @Autowired
    private AuthenticationController authenticationController;

    @Test
    public void register_withValidData_worksCorrectly() throws Exception {
        // Arrange
        final NetId testUser = new NetId("SomeUser");
        final Password testPassword = new Password("password123!");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        final UserRole testUserRole = new UserRole("customer");
        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);

        RegistrationRequestModel model = new RegistrationRequestModel();
        model.setNetId(testUser.toString());
        model.setPassword(testPassword.toString());
        // Act
        ResultActions resultActions = mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model)));
        // Assert
        resultActions.andExpect(status().isOk());

        AppUser savedUser = userRepository.findByNetId(testUser).orElseThrow();

        assertThat(savedUser.getNetId()).isEqualTo(testUser);
        assertThat(savedUser.getPassword()).isEqualTo(testHashedPassword);
        assertThat(savedUser.getUserRole()).isEqualTo(testUserRole);
    }

    @Test
    public void registerNullCheck() throws Exception {
        // Arrange
        final NetId testUser = new NetId("SomeUser");
        final Password testPassword = new Password("password123!");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        final UserRole testUserRole = new UserRole("customer");
        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);

        RegistrationRequestModel model = new RegistrationRequestModel();
        model.setNetId(testUser.toString());
        model.setPassword(testPassword.toString());

        // Assert
        assertThat(authenticationController.register(model)).isNotNull();
    }

    @Test
    public void register_withExistingUser_throwsException() throws Exception {
        // Arrange
        final NetId testUser = new NetId("SomeUser");
        final Password newTestPassword = new Password("password456!");
        final HashedPassword existingTestPassword = new HashedPassword("password123");
        final UserRole testUserRole = new UserRole("customer");
        final List<Long> allergies = new ArrayList<>();
        allergies.add(0L);


        AppUser existingAppUser = new AppUser(testUser, existingTestPassword, testUserRole, allergies);
        userRepository.save(existingAppUser);

        RegistrationRequestModel model = new RegistrationRequestModel();
        model.setNetId(testUser.toString());
        model.setPassword(newTestPassword.toString());

        // Act
        ResultActions resultActions = mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model)));

        // Assert
        resultActions.andExpect(status().isBadRequest());

        AppUser savedUser = userRepository.findByNetId(testUser).orElseThrow();

        assertThat(savedUser.getNetId()).isEqualTo(testUser);
        assertThat(savedUser.getPassword()).isEqualTo(existingTestPassword);
        assertThat(savedUser.getUserRole()).isEqualTo(testUserRole);
    }

    @Test
    public void login_withValidUser_returnsToken() throws Exception {
        // Arrange
        final NetId testUser = new NetId("SomeUser");
        final Password testPassword = new Password("password123!");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        final UserRole testUserRole = new UserRole("customer");
        final List<Long> allergies = new ArrayList<>();
        allergies.add(0L);

        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);
        when(mockAuthenticationManager.authenticate(argThat(authentication ->
                !testUser.toString().equals(authentication.getPrincipal())
                    || !testPassword.toString().equals(authentication.getCredentials())
        ))).thenThrow(new UsernameNotFoundException("User not found"));

        final String testToken = "testJWTToken";
        when(mockJwtUserDetailsService.loadUserByUsername("SomeUser")).thenReturn(null);
        when(mockJwtTokenGenerator.generateToken(null)).thenReturn(testToken);

        AppUser appUser = new AppUser(testUser, testHashedPassword, testUserRole, allergies);
        userRepository.save(appUser);

        AuthenticationRequestModel model = new AuthenticationRequestModel();
        model.setNetId(testUser.toString());
        model.setPassword(testPassword.toString());

        // Act
        ResultActions resultActions = mockMvc.perform(post("/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model)));

        // Assert
        MvcResult result = resultActions
                .andExpect(status().isOk())
                .andReturn();
        AuthenticationResponseModel responseModel = JsonUtil.deserialize(result.getResponse().getContentAsString(),
                AuthenticationResponseModel.class);

        assertThat(responseModel.getToken()).isEqualTo(testToken);
        verify(mockAuthenticationManager).authenticate(argThat(authentication ->
                testUser.toString().equals(authentication.getPrincipal())
                    && testPassword.toString().equals(authentication.getCredentials())));

        //maybe implement check for UserRole in token here?
    }

    @Test
    public void login_withNonexistentUsername_returns403() throws Exception {
        // Arrange
        final String testUser = "SomeUser";
        final String testPassword = "password123!";

        when(mockAuthenticationManager.authenticate(argThat(authentication ->
                testUser.equals(authentication.getPrincipal())
                    && testPassword.equals(authentication.getCredentials())
        ))).thenThrow(new UsernameNotFoundException("User not found"));

        AuthenticationRequestModel model = new AuthenticationRequestModel();
        model.setNetId(testUser);
        model.setPassword(testPassword);

        // Act
        ResultActions resultActions = mockMvc.perform(post("/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model)));

        // Assert
        resultActions.andExpect(status().isForbidden());

        verify(mockAuthenticationManager).authenticate(argThat(authentication ->
                testUser.equals(authentication.getPrincipal())
                    && testPassword.equals(authentication.getCredentials())));

        verify(mockJwtTokenGenerator, times(0)).generateToken(any());
    }

    @Test
    public void login_withInvalidPassword_returns403() throws Exception {
        // Arrange
        final String testUser = "SomeUser";
        final String wrongPassword = "password1234!";
        final String testPassword = "password123!";
        final UserRole testUserRole = new UserRole("customer");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        final List<Long> allergies = new ArrayList<>();
        allergies.add(0L);

        when(mockPasswordEncoder.hash(new Password(testPassword))).thenReturn(testHashedPassword);

        when(mockAuthenticationManager.authenticate(argThat(authentication ->
                testUser.equals(authentication.getPrincipal())
                    && wrongPassword.equals(authentication.getCredentials())
        ))).thenThrow(new BadCredentialsException("Invalid password"));

        AppUser appUser = new AppUser(new NetId(testUser), testHashedPassword, testUserRole, allergies);
        userRepository.save(appUser);

        AuthenticationRequestModel model = new AuthenticationRequestModel();
        model.setNetId(testUser);
        model.setPassword(wrongPassword);

        // Act
        ResultActions resultActions = mockMvc.perform(post("/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model)));

        // Assert
        resultActions.andExpect(status().isUnauthorized());

        verify(mockAuthenticationManager).authenticate(argThat(authentication ->
                testUser.equals(authentication.getPrincipal())
                    && wrongPassword.equals(authentication.getCredentials())));

        verify(mockJwtTokenGenerator, times(0)).generateToken(any());
    }

    @Test
    public void loginUserDisabled() throws Exception {
        // Arrange
        final String testUser = "SomeUser";
        final String testPassword = "password123!";
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");

        when(mockPasswordEncoder.hash(new Password(testPassword))).thenReturn(testHashedPassword);
        when(mockAuthenticationManager.authenticate(argThat(authentication ->
                testUser.equals(authentication.getPrincipal())
                        && testPassword.equals(authentication.getCredentials())
        ))).thenThrow(new DisabledException("User disabled"));

        AuthenticationRequestModel model = new AuthenticationRequestModel();
        model.setNetId(testUser);
        model.setPassword(testPassword);
        // Act
        ResultActions resultActions = mockMvc.perform(post("/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model)));

        // Assert
        resultActions.andExpect(status().isUnauthorized());
        verify(mockAuthenticationManager).authenticate(argThat(authentication ->
                testUser.equals(authentication.getPrincipal())
                        && testPassword.equals(authentication.getCredentials())));
        verify(mockJwtTokenGenerator, times(0)).generateToken(any());
    }

    @Test
    public void registerStoreOrManager_withValidRole_worksCorrectly() throws Exception {
        // Arrange
        final NetId testUser = new NetId("SomeUser");
        final Password testPassword = new Password("password123!");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        final UserRole testUserRole = new UserRole("store");
        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);

        RegistrationSpecialRequestModel model = new RegistrationSpecialRequestModel();
        model.setNetId(testUser.toString());
        model.setPassword(testPassword.toString());
        model.setUserRole(testUserRole.toString());

        // Act
        ResultActions resultActions = mockMvc.perform(post("/registerStoreOrManager")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model)));

        // Assert
        resultActions.andExpect(status().isOk());

        AppUser savedUser = userRepository.findByNetId(testUser).orElseThrow();

        assertThat(savedUser.getNetId()).isEqualTo(testUser);
        assertThat(savedUser.getPassword()).isEqualTo(testHashedPassword);
        assertThat(savedUser.getUserRole()).isEqualTo(testUserRole);
    }

    @Test
    public void registerStoreOrManager_withExistingUser_throwsException() throws Exception {
        // Arrange
        final NetId testUser = new NetId("SomeUser");
        final Password newTestPassword = new Password("password456!");
        final HashedPassword existingTestPassword = new HashedPassword("password123");
        final UserRole testUserRole = new UserRole("store");
        final UserRole existingTestUserRole = new UserRole("manager");
        final List<Long> allergies = new ArrayList<>();
        allergies.add(0L);

        AppUser existingAppUser = new AppUser(testUser, existingTestPassword, existingTestUserRole, allergies);
        userRepository.save(existingAppUser);

        RegistrationSpecialRequestModel model = new RegistrationSpecialRequestModel();
        model.setNetId(testUser.toString());
        model.setPassword(newTestPassword.toString());
        model.setUserRole(testUserRole.toString());

        // Act
        ResultActions resultActions = mockMvc.perform(post("/registerStoreOrManager")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model)));

        // Assert
        resultActions.andExpect(status().isBadRequest());

        AppUser savedUser = userRepository.findByNetId(testUser).orElseThrow();

        assertThat(savedUser.getNetId()).isEqualTo(testUser);
        assertThat(savedUser.getPassword()).isEqualTo(existingTestPassword);
        assertThat(savedUser.getUserRole()).isEqualTo(existingTestUserRole);
    }

    @Test
    public void registerStoreOrManager_withInvalidRole_returnsBadRequest() throws Exception {
        // Arrange
        final NetId testUser = new NetId("SomeUser");
        final Password testPassword = new Password("password123!");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        final UserRole testUserRole = new UserRole("fakeRole");
        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);

        RegistrationSpecialRequestModel model = new RegistrationSpecialRequestModel();
        model.setNetId(testUser.toString());
        model.setPassword(testPassword.toString());
        model.setUserRole(testUserRole.toString());

        // Act
        ResultActions resultActions = mockMvc.perform(post("/registerStoreOrManager")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model)));

        // Assert
        resultActions.andExpect(status().isBadRequest());

        String body = resultActions.andReturn().getResponse().getContentAsString();
        assertThat(body.equals("invalid role!"));
    }

    @Test
    public void registerStoreOrManagerNullCheck() throws Exception {
        // Arrange
        final NetId testUser = new NetId("SomeUser");
        final Password testPassword = new Password("password123!");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        final UserRole testUserRole = new UserRole("manager");
        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);

        RegistrationSpecialRequestModel model = new RegistrationSpecialRequestModel();
        model.setNetId(testUser.toString());
        model.setPassword(testPassword.toString());
        model.setUserRole(testUserRole.toString());

        // Assert
        assertThat(authenticationController.registerStore(model)).isNotNull();
    }

    @Test
    public void getStoresValid() throws Exception {
        AppUser store1 = new AppUser(new NetId("Delft"), new HashedPassword("test"),
                new UserRole("manager"), List.of(0L));
        List<AppUser> stores = new ArrayList<>(List.of(store1));
        when(mockJwtUserDetailsService.getAllStores()).thenReturn(stores);

        ResultActions resultActions = mockMvc.perform(get("/allStores")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));
        resultActions.andExpect(status().isOk());
        MvcResult result = mockMvc.perform(get("/allStores")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")).andReturn();
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).isEqualTo("Below are all available stores:\n"
                + "0- Delft\n"
                + "In order to choose the store you wish to order from, please make the following POST request:\n"
                + "http://localhost:8083/api/basket/setStore/{storeID}\n"
                + "where storeID is the number next to store name on the list above.");
    }

    @Test
    public void getStoresNullCheck() throws Exception {
        AppUser store1 = new AppUser(new NetId("Delft"), new HashedPassword("test"),
                new UserRole("manager"), List.of(0L));
        AppUser store2 = new AppUser(new NetId("Rotterdam"), new HashedPassword("test2"),
                new UserRole("manager"), List.of(0L));
        List<AppUser> stores = new ArrayList<>(List.of(store1, store2));
        when(mockJwtUserDetailsService.getAllStores()).thenReturn(stores);

        assertThat(authenticationController.getAllStores()).isNotNull();
    }

    @Test
    public void verifyStoresValid() throws Exception {
        AppUser store1 = new AppUser(new NetId("Delft"), new HashedPassword("test"),
                new UserRole("manager"), List.of(0L));
        AppUser store2 = new AppUser(new NetId("Rotterdam"), new HashedPassword("test2"),
                new UserRole("manager"), List.of(0L));
        List<AppUser> stores = new ArrayList<>(List.of(store1, store2));
        when(mockJwtUserDetailsService.getAllStores()).thenReturn(stores);

        ResultActions resultActions = mockMvc.perform(get("/verify/0")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));
        resultActions.andExpect(status().isOk());
        MvcResult result = mockMvc.perform(get("/verify/0")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")).andReturn();
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).isEqualTo("true");
    }

    @Test
    public void verifyStoresInvalid() throws Exception {
        AppUser store1 = new AppUser(new NetId("Delft"), new HashedPassword("test"),
                new UserRole("manager"), List.of(0L));
        AppUser store2 = new AppUser(new NetId("Rotterdam"), new HashedPassword("test2"),
                new UserRole("manager"), List.of(0L));
        List<AppUser> stores = new ArrayList<>(List.of(store1, store2));
        when(mockJwtUserDetailsService.getAllStores()).thenReturn(stores);

        ResultActions resultActions = mockMvc.perform(get("/verify/23")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));
        resultActions.andExpect(status().isOk());
        MvcResult result = mockMvc.perform(get("/verify/23")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")).andReturn();
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).isEqualTo("false");
    }

    @Test
    public void verifyStoresNullCheck() throws Exception {
        AppUser store1 = new AppUser(new NetId("Delft"), new HashedPassword("test"),
                new UserRole("manager"), List.of(0L));
        AppUser store2 = new AppUser(new NetId("Rotterdam"), new HashedPassword("test2"),
                new UserRole("manager"), List.of(0L));
        List<AppUser> stores = new ArrayList<>(List.of(store1, store2));
        when(mockJwtUserDetailsService.getAllStores()).thenReturn(stores);

        assertThat(authenticationController.verifyStoreId(0)).isNotNull();
        assertThat(authenticationController.verifyStoreId(23)).isNotNull();
    }

}
