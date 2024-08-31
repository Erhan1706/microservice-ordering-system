package commons;

import java.text.DecimalFormat;
import java.time.LocalDateTime;

@SuppressWarnings("PMD")
public class Basket {

    private String customerId;
    private LocalDateTime time;
    private BasketInfo basketInfo;

    public Basket(String customerId, BasketInfo basketInfo) {
        this.basketInfo = basketInfo;
        this.customerId = customerId;
        this.time = LocalDateTime.now().plusHours(1);
    }

    public Basket(String customerId) {
        this.basketInfo = new BasketInfo();
        this.customerId = customerId;
        this.time = LocalDateTime.now().plusHours(1);
    }

    public BasketInfo getBasketInfo() {
        return basketInfo;
    }

    public LocalDateTime getTime() { return this.time; }

    public void setTime(LocalDateTime time) { this.time = time; }

    public String timeToString() {
        StringBuilder sb = new StringBuilder();

        sb.append(this.time.getMonthValue());
        sb.append("/");
        sb.append(this.time.getDayOfMonth());

        sb.append(" ");

        sb.append(this.time.getHour());
        sb.append(":");
        sb.append(this.time.getMinute());

        return sb.toString();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Pizzas in Basket:\n");
        for (Pizza p : basketInfo.getPizzas()) {
            sb.append("- ").append(p.getName()).append(" €").append(p.getPrice()).append('\n');
        }

        sb.append("\nCoupon applied: ");
        if (basketInfo.getCoupon() == null) {
            sb.append("None");
        } else if (basketInfo.getCoupon().getType() == 'D') {
            sb.append(basketInfo.getCoupon().getCode()).append(" (").append(basketInfo.getCoupon().getRate())
                    .append("% discount coupon)");
        } else if (basketInfo.getCoupon().getType() == 'F') {
            sb.append(basketInfo.getCoupon().getCode()).append(" (Buy-one-get-one-free coupon)");
        } else {
            sb.append(basketInfo.getCoupon().getCode());
        }

        sb.append("\n\n* There is a service fee of €3.00.");

        DecimalFormat df = new DecimalFormat("0.00");
        sb.append("\nTotal price: €").append(df.format(basketInfo.getPrice()));

        return sb.toString();
    }

    public String pizzasToString() {
        StringBuilder sb = new StringBuilder();
        for (Pizza p : basketInfo.getPizzas()) sb.append(p).append('\n');

        return sb.toString();
    }

    public Boolean contains(String pizzaName) {
        for (Pizza pizza : basketInfo.getPizzas()) {
            if (pizza.getName().equals(pizzaName))
                return true;
        }
        return false;
    }

}
