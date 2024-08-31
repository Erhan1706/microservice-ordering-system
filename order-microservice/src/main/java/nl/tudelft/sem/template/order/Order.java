package nl.tudelft.sem.template.order;

import commons.Coupon;
import commons.Pizza;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

public class Order implements Comparable<Order> {

    @Getter
    private final transient String userId;

    @Getter
    private int orderId;

    @Getter
    private String storeId;

    @Getter
    // list of pizzas
    private List<Pizza> pizzas;
    
    @Getter
    private Coupon coupon;

    @Getter
    private final transient float price;

    @Getter
    private transient LocalDateTime finishTime;


    public Order(int orderId, String storeId, String userId, List<Pizza> pizzas, Coupon coupon, float price,
                 LocalDateTime finishTime) {
        this.storeId = storeId;
        this.userId = userId;
        this.orderId = orderId;
        this.pizzas = pizzas;
        this.finishTime = finishTime;
        this.price = price;
        this.coupon = coupon;
    }

    @Override
    public int compareTo(Order o) {
        return finishTime.compareTo(o.finishTime);
    }

    @Override
    public String toString() {
        return "Order{"
                + "userId='" + userId + '\''
                + ", orderId=" + orderId
                + ", storeId='" + storeId + '\''
                + ", pizzas=" + pizzas
                + ", coupon=" + coupon
                + ", price=" + price
                + ", finishTime=" + finishTime
                + '}';
    }
}
