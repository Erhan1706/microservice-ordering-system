package commons.strategies;

import commons.Pizza;
import java.util.List;

public interface PriceStrategy {

    public double calculatePrice(List<Pizza> pizzas);

    public String toString();

    public String getMessage();
}