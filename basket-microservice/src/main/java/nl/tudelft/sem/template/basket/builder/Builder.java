package nl.tudelft.sem.template.basket.builder;

import commons.Ingredient;
import commons.Pizza;

import java.util.List;

public interface Builder {

    void setName(String name);

    void setIngredients(List<Ingredient> ingredients);

    Pizza build();
}