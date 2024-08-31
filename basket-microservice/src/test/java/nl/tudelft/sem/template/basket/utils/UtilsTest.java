package nl.tudelft.sem.template.basket.utils;

import commons.Basket;
import commons.Coupon;
import commons.Ingredient;
import commons.Pizza;
import nl.tudelft.sem.template.basket.builder.PizzaBuilder;
import nl.tudelft.sem.template.basket.services.BasketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class UtilsTest {

    private PizzaBuilder builder;
    private BasketService basketService;

    @BeforeEach
    void setup() {
        builder = new PizzaBuilder();
        basketService = new BasketService();
    }


    @Test
    void fullBuilderTest() {
        builder.setName("4 Cheese");
        Ingredient ingredient = new Ingredient("Cheese", 1.99);
        Ingredient ingredient2 = new Ingredient("Salami", 2.87);
        Ingredient ingredient3 = new Ingredient("Tomato Sauce", 0.99);
        builder.setIngredients(List.of(ingredient, ingredient2, ingredient3));
        Pizza pizza = builder.build();
        assertThat(pizza.getName()).isEqualTo("4 Cheese");
        assertThat(pizza.getIngredients()).containsAll(List.of(ingredient, ingredient2, ingredient3));
    }

    @Test
    void createBasketTest() {
        basketService.createBasket("User");
        assertThat(basketService.getBasket("User")).isNotNull();
    }

    @Test
    void addPizzaToBasketTest() {
        Ingredient ingredient = new Ingredient("Cheese", 1.99);
        Ingredient ingredient2 = new Ingredient("Salami", 2.87);
        Ingredient ingredient3 = new Ingredient("Tomato Sauce", 0.99);
        Pizza pizza = new Pizza("My Pizza", List.of(ingredient, ingredient2, ingredient3));
        basketService.createBasket("User");
        basketService.addPizzaToBasket("User", pizza);
        Basket basket = basketService.getBasket("User");
        assertThat(basket.getBasketInfo().getPizzas().size()).isEqualTo(1);
        assertThat(basket.getBasketInfo().getPizzas().get(0).getName()).isEqualTo("My Pizza");
        assertThat(basket.getBasketInfo().getPizzas().get(0).getIngredients())
                .containsAll(List.of(ingredient, ingredient2, ingredient3));
    }

    @Test
    void removePizzaFromBasketTest() {
        Ingredient ingredient = new Ingredient("Cheese", 1.99);
        Ingredient ingredient2 = new Ingredient("Salami", 2.87);
        Ingredient ingredient3 = new Ingredient("Tomato Sauce", 0.99);
        Pizza pizza = new Pizza("My Pizza", List.of(ingredient, ingredient2, ingredient3));
        basketService.createBasket("User");
        basketService.addPizzaToBasket("User", pizza);
        basketService.removePizzaFromBasket("User", "My Pizza");
        assertThat(basketService.getBasket("User")).isNotNull();
        Basket basket = basketService.getBasket("User");
        assertThat(basket.getBasketInfo().getPizzas().size()).isEqualTo(0);
    }

    @Test
    void applyCouponTest() {
        Ingredient ingredient = new Ingredient("Cheese", 2.00);
        Ingredient ingredient2 = new Ingredient("Salami", 5.00);
        Ingredient ingredient3 = new Ingredient("Tomato Sauce", 1.00);
        Pizza pizza = new Pizza("My Pizza", List.of(ingredient, ingredient2, ingredient3));
        basketService.createBasket("User");
        Coupon coupon = new Coupon("EXL01", 'D', 30, false);
        assertThat(basketService.applyCouponToBasket("User", coupon)).isTrue();

        // Apply a coupon with less discount
        Coupon coupon3 = new Coupon("EXL01", 'D', 25, false);
        assertThat(basketService.applyCouponToBasket("User", coupon3)).isFalse();
    }

    @Test
    void removeCouponTest() {
        Ingredient ingredient = new Ingredient("Cheese", 2.00);
        Ingredient ingredient2 = new Ingredient("Salami", 5.00);
        Ingredient ingredient3 = new Ingredient("Tomato Sauce", 1.00);
        Pizza pizza = new Pizza("My Pizza", List.of(ingredient, ingredient2, ingredient3));
        basketService.createBasket("User");
        Coupon coupon = new Coupon("EXL01", 'D', 30, false);
        assertThat(basketService.applyCouponToBasket("User", coupon)).isTrue();
        basketService.removeCouponFromBasket("User");
        Basket basket = basketService.getBasket("User");
        assertThat(basket.getBasketInfo().getCoupon()).isNull();
    }
}
