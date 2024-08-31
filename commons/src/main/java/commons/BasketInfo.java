package commons;

import java.util.ArrayList;
import java.util.List;

public class BasketInfo {

    private List<Pizza> pizzas;
    private double price;
    private Coupon coupon;
    private int storeId;

    @SuppressWarnings("PMD")
    public BasketInfo() {
        this.pizzas = new ArrayList<>();
        this.price = 3.0;
        this.coupon = null;
        this.storeId = -1;
    }

    @SuppressWarnings("PMD")
    public BasketInfo(List<Pizza> pizzas, double price) {
        this.pizzas = pizzas;
        this.price = price;
        this.coupon = null;
        this.storeId = -1;
    }

    @SuppressWarnings("PMD")
    public BasketInfo(List<Pizza> pizzas, double price, int storeId) {
        this.pizzas = pizzas;
        this.price = price;
        this.coupon = null;
        this.storeId = storeId;
    }

    public BasketInfo(List<Pizza> pizzas, double price, Coupon coupon) {
        this.pizzas = pizzas;
        this.price = price;
        this.coupon = coupon;
    }

    public void setPizzas(List<Pizza> pizzas) {
        this.pizzas = pizzas;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setCoupon(Coupon coupon) {
        this.coupon = coupon;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public List<Pizza> getPizzas() {
        return pizzas;
    }

    public double getPrice() {
        return price;
    }

    public Coupon getCoupon() {
        return coupon;
    }

    public int getStoreId() {
        return storeId;
    }


}
