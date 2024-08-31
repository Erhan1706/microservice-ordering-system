package nl.tudelft.sem.template.basket.services;


import commons.Basket;
import commons.BasketInfo;
import commons.Coupon;
import commons.Pizza;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.Tuple;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("PMD")
@Service
public class BasketService {
    private Map<String, Basket> baskets = new HashMap<>();

    /**
     * Creates a new basket for the customer.
     *
     * @param customerId the username of the customer that owns the basket
     */
    public void createBasket(String customerId) {
        Basket basket = new Basket(customerId, new BasketInfo());
        baskets.put(customerId, basket);
    }


    /**
     * Retrieves the basket of the customer, called when CHECKOUT.
     * Removes the checkout'ed basket from baskets map
     *
     * @param customerId the id of the customer
     * @return the basket of the customer
     */
    public Basket getBasket(String customerId) {
        Basket basket = baskets.get(customerId);
        return basket;
    }

    /**
     * Removes the basket of the customer from active baskets collection.
     *
     * @param customerId the ID of the customer
     */
    public void removeBasket(String customerId) {
        baskets.remove(customerId);
    }

    public void calculatePrice(String customerId) {
        Basket basket = baskets.get(customerId);
        double price = 3.0;

        if (basket.getBasketInfo().getCoupon() == null) {
            for (Pizza p : basket.getBasketInfo().getPizzas()) {
                price += p.getPrice();
            }
            basket.getBasketInfo().setPrice(price);
        } else {
            price = basket.getBasketInfo().getCoupon().calculatePrice(basket.getBasketInfo()
                .getPizzas());
            basket.getBasketInfo().setPrice(price);
        }
    }

    /**
     * Adds the passed pizza to the basket of the customer.
     *
     * @param customerId ID of the owner of the basket
     * @param pizza      the pizza to be added
     */
    public void addPizzaToBasket(String customerId, Pizza pizza) {
        Basket basket = baskets.get(customerId);
        basket.getBasketInfo().getPizzas().add(pizza);
        calculatePrice(customerId);
    }

    public void removePizzaFromBasket(String customerId, String pizzaName) {
        Basket basket = baskets.get(customerId);
        for (Pizza pizza : basket.getBasketInfo().getPizzas()) {
            if (pizza.getName().equals(pizzaName)) {
                basket.getBasketInfo().getPizzas().remove(pizza);
                return;
            }
        }
        calculatePrice(customerId);
    }

    /**
     * Applies coupon to the basket.
     * If there is already a coupon that has been applied, the prices are compared and the cheaper coupon is applied.
     *
     * @param customerId ID of the owner of the basket
     * @param coupon     the coupon to be added
     * @return true if the coupon provided has been applied, false if it hasn't
     */
    public boolean applyCouponToBasket(String customerId, Coupon coupon) {
        Basket basket = baskets.get(customerId);
        Coupon curr = basket.getBasketInfo().getCoupon();
        double newPrice = coupon.calculatePrice(basket.getBasketInfo().getPizzas());

        if (curr == null) {
            basket.getBasketInfo().setPrice(newPrice);
            basket.getBasketInfo().setCoupon(coupon);
            return true;
        } else {
            if (newPrice < basket.getBasketInfo().getPrice()) {
                basket.getBasketInfo().setPrice(newPrice);
                basket.getBasketInfo().setCoupon(coupon);
                return true;
            } else return false;
        }
    }

    /**
     * Removes a coupon from basket and re-calculates the price.
     *
     * @param customerId ID of the owner of the basket
     */
    public void removeCouponFromBasket(String customerId) {
        Basket basket = baskets.get(customerId);
        basket.getBasketInfo().setCoupon(null);
        calculatePrice(customerId);
    }

    /**
     * Sets the store preference of the customer to the basket's field.
     *
     * @param customerId the owner of the basket's id
     * @param storeId    the id of the store to be ordered from
     * @return Message
     */
    public String setStorePreference(String customerId, int storeId) {
        Basket basket = baskets.get(customerId);
        basket.getBasketInfo().setStoreId(storeId);
        return "Store preference saved.";
    }
}
