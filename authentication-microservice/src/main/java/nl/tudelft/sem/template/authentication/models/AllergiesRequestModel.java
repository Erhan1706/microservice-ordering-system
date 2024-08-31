package nl.tudelft.sem.template.authentication.models;

import java.util.List;

import lombok.Data;

@Data
public class AllergiesRequestModel {
    private List<Long> allergies;
}
