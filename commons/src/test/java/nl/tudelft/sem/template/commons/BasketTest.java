package nl.tudelft.sem.template.commons;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import commons.Basket;
import commons.BasketInfo;
import commons.Coupon;
import commons.Pizza;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BasketTest {
    private Basket basket;

    @BeforeEach
    void setup() {
        Coupon c = new Coupon("coupon", 'R', 2.0, false);
        Pizza a = new Pizza();
        List<Pizza> pizzaList = Arrays.asList(a);
        BasketInfo basketInfo = new BasketInfo(pizzaList, 2.0, c);
        basket = new Basket("custId", basketInfo);
    }

    @Test
    void constructor() {
        Basket b = new Basket("custId");
        assertNotNull(b);
    }

    @Test
    void getPizzas() {

        Pizza a = new Pizza();
        List<Pizza> pizzaList = Arrays.asList(a);
        assertThat(basket.getBasketInfo().getPizzas()).isEqualTo(pizzaList);
    }

    @Test
    void getPrice() {
        double price = 2.0;
        assertThat(basket.getBasketInfo().getPrice()).isEqualTo(price);
    }

    @Test
    void getCoupon() {
        Coupon c = new Coupon("coupon", 'R', 2.0, false);
        assertThat(basket.getBasketInfo().getCoupon()).isEqualTo(c);
    }

    @Test
    void setPrice() {
        assertThat(basket.getBasketInfo().getPrice()).isEqualTo(2.0);
        basket.getBasketInfo().setPrice(3.0);
        assertThat(basket.getBasketInfo().getPrice()).isEqualTo(3.0);
    }

    @Test
    void setCoupon() {
        Coupon c = new Coupon("coupon", 'R', 2.0, false);
        assertThat(basket.getBasketInfo().getCoupon()).isEqualTo(c);

        Coupon r = new Coupon("coupon2", 'P', 3.0, true);
        basket.getBasketInfo().setCoupon(r);
        assertThat(basket.getBasketInfo().getCoupon()).isEqualTo(r);
    }

    @Test
    void testToString() {
        assertThat(basket.toString()).contains("Pizzas in Basket:\n");
    }

    @Test
    void pizzasToString() {
        assertThat(basket.pizzasToString()).contains("Pizza");
    }

    @Test
    void contains() {
        Pizza a = new Pizza("Pizza", Arrays.asList());
        BasketInfo info = new BasketInfo(Arrays.asList(a), 3.0);
        Basket b2 = new Basket("customer2", info);
        assertTrue(b2.contains("Pizza"));
    }
}
