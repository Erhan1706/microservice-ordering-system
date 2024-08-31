package nl.tudelft.sem.template.authentication.controllers;

import nl.tudelft.sem.template.authentication.authentication.JwtTokenGenerator;
import nl.tudelft.sem.template.authentication.authentication.JwtUserDetailsService;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.valueobjects.NetId;
import nl.tudelft.sem.template.authentication.domain.user.valueobjects.Password;
import nl.tudelft.sem.template.authentication.domain.user.services.RegistrationService;
import nl.tudelft.sem.template.authentication.domain.user.valueobjects.UserRole;
import nl.tudelft.sem.template.authentication.models.AuthenticationRequestModel;
import nl.tudelft.sem.template.authentication.models.AuthenticationResponseModel;
import nl.tudelft.sem.template.authentication.models.RegistrationRequestModel;
import nl.tudelft.sem.template.authentication.models.RegistrationSpecialRequestModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@SuppressWarnings("PMD")
@RestController
public class AuthenticationController {

    private final transient AuthenticationManager authenticationManager;

    private final transient JwtTokenGenerator jwtTokenGenerator;

    private final transient JwtUserDetailsService jwtUserDetailsService;

    private final transient RegistrationService registrationService;

    /**
     * Instantiates a new UsersController.
     *
     * @param authenticationManager the authentication manager
     * @param jwtTokenGenerator     the token generator
     * @param jwtUserDetailsService the user service
     * @param registrationService   the registration service
     */
    @Autowired
    public AuthenticationController(AuthenticationManager authenticationManager,
                                    JwtTokenGenerator jwtTokenGenerator,
                                    JwtUserDetailsService jwtUserDetailsService,
                                    RegistrationService registrationService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenGenerator = jwtTokenGenerator;
        this.jwtUserDetailsService = jwtUserDetailsService;
        this.registrationService = registrationService;
    }

    /**
     * Endpoint for authentication.
     *
     * @param request The login model
     * @return JWT token if the login is successful
     * @throws Exception if the user does not exist or the password is incorrect
     */
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponseModel> authenticate(
            @RequestBody AuthenticationRequestModel request)
            throws Exception {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getNetId(),
                            request.getPassword()));
        } catch (DisabledException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", e);
        }

        final UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(request.getNetId());
        final String jwtToken = jwtTokenGenerator.generateToken(userDetails);
        return ResponseEntity.ok(new AuthenticationResponseModel(jwtToken));
    }

    /**
     * Endpoint for registration.
     *
     * @param request The registration model
     * @return 200 OK if the registration is successful
     * @throws Exception if a user with this netid already exists
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegistrationRequestModel request) throws Exception {
        try {
            NetId netId = new NetId(request.getNetId());
            Password password = new Password(request.getPassword());
            //role is default customer on this endpoint
            UserRole userRole = new UserRole("customer");
            registrationService.registerUser(netId, password, userRole);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/registerStoreOrManager")
    public ResponseEntity<String> registerStore(@RequestBody RegistrationSpecialRequestModel request) throws Exception {
        try {
            NetId netId = new NetId(request.getNetId());
            Password password = new Password(request.getPassword());
            if (!(request.getUserRole().equals("store") || request.getUserRole().equals("manager"))) {
                return ResponseEntity.badRequest().body("invalid role!");
            }
            UserRole userRole = new UserRole(request.getUserRole());
            registrationService.registerUser(netId, password, userRole);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        return ResponseEntity.ok().build();
    }

    /**
     * GET endpoint for a list-view of all stores in the database.
     * Has an additional message to the customer explaining how to set preferred store
     *
     * @return Stores list and an explanation
     */
    @GetMapping("/allStores")
    public ResponseEntity<String> getAllStores() {
        StringBuilder sb = new StringBuilder();
        sb.append("Below are all available stores:\n");
        for (AppUser store : jwtUserDetailsService.getAllStores()) {
            sb.append(store.getId()).append("- ").append(store.getNetId()).append("\n");
        }
        sb.append("In order to choose the store you wish to order from, please make the following POST request:\n")
              .append("http://localhost:8083/api/basket/setStore/{storeID}\n")
              .append("where storeID is the number next to store name on the list above.");
        return ResponseEntity.ok(sb.toString());
    }

    /**
     * GET endpoint that receives the customer input storeID and verifies if there exists a store with such storeID.
     *
     * @param storeId the customer input for preferred store
     * @return TRUE if there is, and vice-versa
     */
    @GetMapping("/verify/{storeID}")
    public ResponseEntity<Boolean> verifyStoreId(@PathVariable("storeID") int storeId) {
        boolean result = false;
        for (AppUser user : jwtUserDetailsService.getAllStores()) {
            if (user.getId() == storeId) {
                result = true;
            }
        }
        if (result) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.ok(false);
        }
    }
}
