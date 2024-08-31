package nl.tudelft.sem.template.commons;

import commons.Ingredient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IngredientTest {
    private Ingredient cheese;

    @BeforeEach
    public void setup() {
        String name = "Cheese";
        Double price = 9.99;
        cheese = new Ingredient(name, price);
    }

    @Test
    public void testGetId() {
        assertThat(cheese.getId()).isNull();
    }

    @Test
    public void testGetName() {
        assertThat(cheese.getName()).isEqualTo("Cheese");
    }

    @Test
    public void testGetPrice() {
        assertThat(cheese.getPrice()).isEqualTo(9.99);
    }

    @Test
    public void testSetName() {
        String newName = "Molded cheese";
        cheese.setName(newName);
        assertThat(cheese.getName()).isEqualTo("Molded cheese");
    }

    @Test
    public void testSetPrice() {
        Double newPrice = 10.0;
        cheese.setPrice(newPrice);
        assertThat(cheese.getPrice()).isEqualTo(10.00);
    }

    @Test
    public void testEquals1() {
        Ingredient newIngredient = new Ingredient("Pepperoni", 9.99);
        assertThat(cheese.equals(newIngredient)).isFalse();
    }

    @Test
    public void testEquals2() {
        Ingredient newIngredient = new Ingredient("Cheese", 10.00);
        assertThat(cheese.equals(newIngredient)).isFalse();
    }

    @Test
    public void testEqualsNull() {
        Ingredient newIngredient = new Ingredient(null, null);
        newIngredient.setPrice(cheese.getPrice());
        assertThat(cheese.equals(newIngredient)).isFalse();
    }

    @Test
    public void testEqualsNull2() {
        Ingredient newIngredient = new Ingredient(null, null);
        newIngredient.setPrice(cheese.getPrice());
        newIngredient.setName(cheese.getName());
        assertThat(cheese.equals(newIngredient)).isTrue();
    }

    @Test
    public void testHash() {
        Ingredient newIngredient = new Ingredient(null, null);
        newIngredient.setPrice(cheese.getPrice());
        newIngredient.setName(cheese.getName());
        assertThat(cheese.hashCode() == newIngredient.hashCode()).isTrue();
    }

    @Test
    public void testHash2() {
        Ingredient newIngredient = new Ingredient(null, null);
        newIngredient.setPrice(cheese.getPrice());
        assertThat(cheese.hashCode() == newIngredient.hashCode()).isFalse();
    }

}
