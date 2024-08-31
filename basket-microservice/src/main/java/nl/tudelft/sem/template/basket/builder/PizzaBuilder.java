package nl.tudelft.sem.template.basket.builder;

import commons.Ingredient;
import commons.Pizza;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Pizza Builder to build default/custom pizzas that will be added to a basket.
 */
@Component
public class PizzaBuilder implements Builder {

    private transient String name;
    private transient List<Ingredient> ingredients;

    public void setName(String name) {
        this.name = name;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public Pizza build() {
        return new Pizza(this.name, this.ingredients);
    }
}