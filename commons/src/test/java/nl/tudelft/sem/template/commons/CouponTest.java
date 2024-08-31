package nl.tudelft.sem.template.commons;

import commons.Coupon;
import commons.Ingredient;
import commons.Pizza;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CouponTest {

    private Coupon discount;
    private Coupon getOneFree;
    private final Ingredient cheese = new Ingredient("Cheese", 3.99);
    private final Ingredient pepperoni = new Ingredient("Pepperoni", 5.99);
    private final Ingredient salami = new Ingredient("Salami", 4.99);

    @BeforeEach
    void setup() {
        discount = new Coupon("DSCT01", 'D', 50, false);
        getOneFree = new Coupon("BOGO02", 'F', 50, true);
    }

    @Test
    void testFirstConstructor() {
        Coupon coupon = new Coupon("TEST01", 'D', 30, false);
        assertNotNull(coupon);
    }

    @Test
    void testSecondConstructor() {
        Coupon coupon = new Coupon("TEST02");
        assertNotNull(coupon);
    }

    @Test
    void testThirdConstructor() {
        Coupon coupon = new Coupon("TEST03", 'D');
        assertNotNull(coupon);
    }

    @Test
    void testFourthConstructor() {
        Coupon coupon = new Coupon("TEST04", false);
        assertNotNull(coupon);
    }

    @Test
    void testFifthConstructor() {
        Coupon coupon = new Coupon("TEST01", 'D', false);
        assertNotNull(coupon);
    }

    @Test
    void testGetCode() {
        assertThat(discount.getCode().equals("DSCT01")).isTrue();
        assertThat(getOneFree.getCode().equals("BOGO02")).isTrue();
    }

    @Test
    void testGetType() {
        assertThat(discount.getType() == 'D').isTrue();
        assertThat(getOneFree.getType() == 'F').isTrue();
    }

    @Test
    void testGetRate() {
        assertThat(discount.getRate() == 50.0).isTrue();
        assertThat(getOneFree.getRate() == 50.0).isFalse();
        assertThat(getOneFree.getRate() == 0).isTrue();
    }

    @Test
    void testIsLimitedTime() {
        assertThat(discount.isLimitedTime()).isFalse();
        assertThat(getOneFree.isLimitedTime()).isTrue();
    }

    @Test
    void testCalculatePrice() {
        List<Ingredient> firstIngredients = new ArrayList<>();
        firstIngredients.add(cheese);
        firstIngredients.add(pepperoni);
        Pizza margherita = new Pizza("Margherita", firstIngredients);

        List<Ingredient> secondIngredients = new ArrayList<>();
        secondIngredients.add(pepperoni);
        secondIngredients.add(salami);
        Pizza meat = new Pizza("Meat Pizza", secondIngredients);
        List<Pizza> pizzas = new ArrayList<>();
        pizzas.add(margherita);
        pizzas.add(meat);

        assertThat(discount.calculatePrice(pizzas)
                == (margherita.getPrice() + meat.getPrice()) * 0.5 + 3).isTrue();
        assertThat(getOneFree.calculatePrice(pizzas)
                == Math.max(margherita.getPrice(), meat.getPrice()) + 3).isTrue();
    }

}
