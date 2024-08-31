package nl.tudelft.sem.template.commons;

import commons.Ingredient;
import commons.Pizza;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

public class PizzaTest {
    private Ingredient cheese = new Ingredient("Cheese", 3.99);
    private Ingredient pepperoni = new Ingredient("Pepperoni", 5.99);
    private Pizza pizza;
    private Double tip = 3.00;

    @BeforeEach
    public void setup() {
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(cheese);
        ingredients.add(pepperoni);
        pizza = new Pizza("Pepperoni Pizza", ingredients);
    }

    @Test
    public void testPrice() {
        Double expectedPrice = cheese.getPrice() + pepperoni.getPrice() + tip;
        assertThat(pizza.getPrice()).isEqualTo(expectedPrice);
    }

    @Test
    public void testGetIngredients() {
        Boolean result = pizza.getIngredients().contains(cheese) && pizza.getIngredients().contains(pepperoni);
        assertThat(result).isTrue();
    }

    @Test
    public void testGetIngredients2() {
        int oldLength = pizza.getIngredients().size();
        pizza.getIngredients().remove(cheese);
        assertThat(oldLength - 1).isEqualTo(pizza.getIngredients().size());
    }

    @Test
    public void testPrice2() {
        Double oldPrice = pizza.getPrice();
        List<Ingredient> ingredients = pizza.getIngredients();
        Ingredient pineapple = new Ingredient("Pineapple", 2.00);
        ingredients.add(pineapple);
        Pizza newPizza = new Pizza("Hawaii", ingredients);
        assertThat(newPizza.getPrice()).isEqualTo(oldPrice + pineapple.getPrice());
    }

    @Test
    public void testGetName() {
        assertThat(pizza.getName()).isEqualTo("Pepperoni Pizza");
    }

    @Test
    public void testSetName() {
        String newName = "Zort Pizza";
        pizza.setName(newName);
        assertThat(pizza.getName()).isEqualTo(newName);
    }

    @Test
    public void testEquals1() {
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(cheese);
        ingredients.add(pepperoni);
        Pizza pizza2 = new Pizza("Pepperoni Pizza", ingredients);
        assertThat(pizza.equals(pizza2)).isTrue();
    }

    @Test
    public void testEqualsIngredient() {
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(pepperoni);
        Pizza pizza2 = new Pizza("Pepperoni Pizza", ingredients);
        assertThat(pizza.equals(pizza2)).isFalse();
    }

    @Test
    public void testEqualsName() {
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(cheese);
        ingredients.add(pepperoni);
        Pizza pizza2 = new Pizza("PepperoniPizza", ingredients);
        assertThat(pizza.equals(pizza2)).isFalse();
    }

    @Test
    public void testHash1() {
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(cheese);
        ingredients.add(pepperoni);
        Pizza pizza2 = new Pizza("Pepperoni Pizza", ingredients);
        assertThat(pizza.hashCode() == pizza2.hashCode()).isTrue();
    }

    @Test
    public void testHash2() {
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(cheese);
        ingredients.add(pepperoni);
        Pizza pizza2 = new Pizza("PepperoniPizza", ingredients);
        assertThat(pizza.hashCode() == pizza2.hashCode()).isFalse();
    }

    @Test
    public void testHash3() {
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(pepperoni);
        Pizza pizza2 = new Pizza("Pepperoni Pizza", ingredients);
        assertThat(pizza.hashCode() == pizza2.hashCode()).isFalse();
    }


}
