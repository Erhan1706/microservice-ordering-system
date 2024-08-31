package commons.strategies;

import commons.Pizza;
import java.util.List;

/**
 * A child class of PriceStrategy for custom coupons.
 * Contains the list of pizzas that this coupon is valid for,
 * and the new price of the combination of pizzas.
 */
@SuppressWarnings("PMD")
public class CustomStrategy implements PriceStrategy {

    /**
     * The combination of pizzas that this coupon is valid for.
     */
    List<Pizza> combination;

    /**
     * The discounted price.
     * For example, if this coupon is for pizza A and B,
     * the price for pizza A + pizza B is replaced by this newPrice.
     */
    double newPrice;

    public CustomStrategy(List<Pizza> combination, double newPrice) {
        this.combination = combination;
        this.newPrice = newPrice;
    }

    /**
     * First calculates the original price of the list.
     * Then this method checks if the list contains all the pizzas that this coupon is valid for,
     * and calculates the new price by subtracting the original prices and adding the discounted price.
     *
     * @param pizzas the list of pizzas in the Basket.
     * @return the new price after the coupon is applied. Returns the same price if this coupon is not valid for this basket.
     */
    @Override
    public double calculatePrice(List<Pizza> pizzas) {
        double price = 3.0;

        for (Pizza p : pizzas) price += p.getPrice();

        double discount = newPrice;

        for (Pizza p : combination) {
            if (!pizzas.contains(p)) return price;
            else discount -= p.getPrice();
        }

        return price + discount;
    }

    /**
     * String representation which is used when storing this class in Coupon database.
     * Has character 'C' to indicate that this coupon is a custom coupon,
     * followed by list of pizzas in combination and the newPrice at the end.
     *
     * @return a String representation of this class.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("C ");

        for (Pizza p : combination) sb.append(p.getName()).append(" ");
        sb.append(newPrice);

        return sb.toString();
    }

    public String getMessage() {
        return "Coupon has been applied.\n";
    }
}
