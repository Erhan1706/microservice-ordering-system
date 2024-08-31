package nl.tudelft.sem.template.basket.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class IngredientRequestModel {

    private String name;
    private Double price;
}
