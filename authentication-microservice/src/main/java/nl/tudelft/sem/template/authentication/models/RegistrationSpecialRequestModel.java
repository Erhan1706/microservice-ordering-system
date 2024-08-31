package nl.tudelft.sem.template.authentication.models;

import lombok.Data;

/**
 * Model representing a registration request for a non-customer role.
 */
@Data
public class RegistrationSpecialRequestModel {
    private String netId;
    private String password;
    private String userRole;
}
