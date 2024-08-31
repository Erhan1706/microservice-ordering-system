package commons.strategies;

import commons.Pizza;

import java.text.DecimalFormat;
import java.util.List;

/**
 * A child class of PriceStrategy for discount coupons.
 * Takes a double which represents the discount rate.
 */
@SuppressWarnings("PMD")
public class DiscountStrategy implements PriceStrategy {

    /**
     * Discount rate of the coupon.
     */

    double rate;
    DecimalFormat df = new DecimalFormat("0.00");

    public DiscountStrategy(double rate) {
        this.rate = rate;
    }

    /**
     * Calculates the new price of the pizzas
     * by applying the discount to each pizza's price and aggregating them.
     *
     * @param pizzas the list of pizzas in the Basket.
     * @return the price after the discount is applied.
     */
    @Override
    public double calculatePrice(List<Pizza> pizzas) {
        double newPrice = 3.0;

        for (Pizza p : pizzas) newPrice += p.getPrice() - p.getPrice() * (rate / 100);

        return newPrice;
    }

    /**
     * String representation which is used when storing this class in Coupon database.
     * Has character 'D' at the beginning to indicate that this is a discount coupon,
     * followed by the discount rate.
     *
     * @return the String representation of this class.
     */
    public String toString() {
        return "D " + df.format(rate);
    }

    public String getMessage() {
        return rate + "% discount coupon has been applied.\n";
    }

}
