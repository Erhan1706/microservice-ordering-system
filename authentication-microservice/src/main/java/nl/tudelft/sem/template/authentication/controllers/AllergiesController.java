package nl.tudelft.sem.template.authentication.controllers;

import java.util.List;

import commons.authentication.AuthenticationManager;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.domain.user.valueobjects.NetId;
import nl.tudelft.sem.template.authentication.models.AllergiesRequestModel;
import nl.tudelft.sem.template.authentication.models.AllergiesResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controller that adds or updates allergies.
 */
@SuppressWarnings("PMD")
@RestController
public class AllergiesController {

    private final UserRepository us;
    private final AuthenticationManager am;

    /**
    * Constructor for the controller.
    *
    * @param us UserRepository
    * @param am Authentication Manager used to extract information from token
    */
    @Autowired
    public AllergiesController(UserRepository us, AuthenticationManager am) {
        this.us = us;
        this.am = am;
    }

    /**
     * Post mapping for adding allergies through token.
     *
     * @param ar Model for the request.
     * @return returns Response Entity ok if all went good.
     * @throws Exception Bad Request if user isn't found in database.
     */
    @PostMapping("/addAllergies")
    public ResponseEntity addAllergy(@RequestBody AllergiesRequestModel ar) throws Exception {
        if (us.findByNetId(new NetId(am.getNetId())).isPresent()) {
            AppUser user = us.findByNetId(new NetId(am.getNetId())).get();
            user.addAllergies(ar.getAllergies());
            us.save(user);
            return ResponseEntity.ok().build();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token");
        }

    }

    /**
     * Get mapping for all allergies using token.
     *
     * @return returns Response Entity with the response model which is a
     * @throws Exception if user isn't found in database.
     */
    @GetMapping("/getAllergies")
    public ResponseEntity<AllergiesResponseModel> getAllergies() throws Exception {
        String netId = am.getNetId();

        if (us.findByNetId(new NetId(am.getNetId())).isPresent()) {
            AppUser user = us.findByNetId(new NetId(am.getNetId())).get();
            List<Long> allergies = user.getAllergies();
            return ResponseEntity.ok(new AllergiesResponseModel(allergies));

        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token");
        }
    }
}
