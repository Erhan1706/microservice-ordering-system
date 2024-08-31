package commons.strategies;

import commons.Pizza;
import java.util.Collections;
import java.util.List;

/**
 * A child class of PriceStrategy for buy-one-get-one-free coupons.
 */
@SuppressWarnings("PMD")
public class FreeStrategy implements PriceStrategy {

    public FreeStrategy() {}


    /**
     * Calculates the new price of the Basket after this coupon is applied.
     * First looks at how many pizzas are stored in the Basket (i.e., number of pizzas in the list)
     * and sorts the list by increasing order of price.
     * Calculates the new price by adding the prices of all the pizzas, but skipping the first n pizzas.
     *
     * @param pizzas the list of pizzas in the basket.
     * @return the new price after this coupon is applied.
     */
    @Override
    public double calculatePrice(List<Pizza> pizzas) {
        int n = pizzas.size() / 2;  // number of pizzas that are free
        double newPrice = 3.0;

        Collections.sort(pizzas, (o1, o2) -> Double.compare(o1.getPrice(), o2.getPrice()));

        for (Pizza p : pizzas) {
            if (n > 0) {        // skip the first n pizzas, because these are free.
                n--;
                continue;
            }

            newPrice += p.getPrice();
        }

        return newPrice;
    }

    /**
     * String representation which is used when storing this class in Coupon database.
     * Has character 'F' to indicate that this is a buy-one-get-one-free coupon.
     *
     * @return the String representation of this class (i.e., "F")
     */
    public String toString() {
        return "F";
    }

    public String getMessage() {
        return "Buy-one-get-one-free coupon has been applied.\n";
    }
}
