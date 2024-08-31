package nl.tudelft.sem.template.commons;

import commons.Ingredient;
import commons.Pizza;
import commons.strategies.DiscountStrategy;
import commons.strategies.FreeStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class StrategyTest {

    private final Ingredient cheese = new Ingredient("Cheese", 3.99);
    private final Ingredient pepperoni = new Ingredient("Pepperoni", 5.99);
    private final Ingredient salami = new Ingredient("Salami", 4.99);
    private Pizza pizzaOne;
    private Pizza pizzaTwo;
    private Pizza pizzaThree;
    private Pizza pizzaFour;
    private Pizza pizzaFive;
    private List<Pizza> pizzas = new ArrayList<>();
    private DiscountStrategy discount;
    private FreeStrategy free;
    private final DecimalFormat df = new DecimalFormat("0.00");

    @BeforeEach
    void setup() {
        List<Ingredient> one = new ArrayList<>();
        one.add(cheese);
        pizzaOne = new Pizza("one", one);

        List<Ingredient> two = new ArrayList<>();
        two.add(pepperoni);
        pizzaTwo = new Pizza("two", two);

        List<Ingredient> three = new ArrayList<>();
        three.add(salami);
        pizzaThree = new Pizza("three", three);

        List<Ingredient> four = new ArrayList<>();
        four.add(cheese);
        four.add(pepperoni);
        pizzaFour = new Pizza("four", four);

        List<Ingredient> five = new ArrayList<>();
        five.add(cheese);
        five.add(pepperoni);
        five.add(salami);
        pizzaFive = new Pizza("five", five);

        pizzas.add(pizzaOne);
        pizzas.add(pizzaTwo);
        pizzas.add(pizzaThree);
        pizzas.add(pizzaFour);
        pizzas.add(pizzaFive);
    }

    @Test
    void testDiscountConstructor() {
        discount = new DiscountStrategy(50);
        assertNotNull(discount);
    }

    @Test
    void testDiscountCalculatePrice1() {
        discount = new DiscountStrategy(30);
        double price = discount.calculatePrice(pizzas);
        double calculated = (pizzaOne.getPrice() + pizzaTwo.getPrice() + pizzaThree.getPrice()
                + pizzaFour.getPrice() + pizzaFive.getPrice()) * 0.7 + 3;

        assertThat(df.format(price).equals(df.format(calculated))).isTrue();
    }

    @Test
    void testDiscountCalculatePrice2() {
        discount = new DiscountStrategy(50);
        pizzas.remove(pizzaFive);
        pizzas.remove(pizzaFour);
        assertThat(discount.calculatePrice(pizzas)
                == (pizzaOne.getPrice() + pizzaTwo.getPrice() + pizzaThree.getPrice())
                * 0.5 + 3).isTrue();
    }

    @Test
    void testDiscountCalculatePrice3() {
        discount = new DiscountStrategy(100);
        assertThat(discount.calculatePrice(pizzas) == 3).isTrue();
    }

    @Test
    void testDiscountToString() {
        discount = new DiscountStrategy(3.141592);
        assertThat(discount.toString().equals("D 3.14")).isTrue();
    }

    @Test
    void testFreeConstructor() {
        free = new FreeStrategy();
        assertNotNull(free);
    }

    @Test
    void testFreeCalculatePrice1() {
        free = new FreeStrategy();
        assertThat(free.calculatePrice(pizzas)
                == (pizzaTwo.getPrice() + pizzaFour.getPrice() + pizzaFive.getPrice()) + 3).isTrue();
    }

    @Test
    void testFreeCalculatePrice2() {
        free = new FreeStrategy();
        pizzas.remove(pizzaFive);
        assertThat(free.calculatePrice(pizzas)
                == (pizzaTwo.getPrice() + pizzaFour.getPrice()) + 3).isTrue();
    }

    @Test
    void testFreeCalculatePrice3() {
        free = new FreeStrategy();
        List<Pizza> onlyOne = new ArrayList<>();
        onlyOne.add(pizzaOne);
        assertThat(free.calculatePrice(onlyOne) == pizzaOne.getPrice() + 3).isTrue();
    }

    @Test
    void testFreeToString() {
        free = new FreeStrategy();
        assertThat(free.toString().equals("F")).isTrue();
    }
}
