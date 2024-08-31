package nl.tudelft.sem.template.basket.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class PizzaRequestModel {

    private String name;
    private List<String> ingredients;
}
