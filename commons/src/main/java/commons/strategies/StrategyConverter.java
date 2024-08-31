package commons.strategies;


import commons.Pizza;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("PMD")
@Converter
public class StrategyConverter implements AttributeConverter<PriceStrategy, String> {
    @Override
    public String convertToDatabaseColumn(PriceStrategy attribute) { return attribute.toString(); }

    @Override
    public PriceStrategy convertToEntityAttribute(String dbData) {
        char type = dbData.charAt(0);

        if (type == 'D') return new DiscountStrategy(Double.parseDouble(dbData.split(" ")[1]
            .replace(",", ".")));
        else if (type == 'F') return new FreeStrategy();
        else {
            List<Pizza> pizzas = new ArrayList<>();

            String[] names = dbData.split(" ");
            for (int i = 0; i < names.length - 1; i++) {
                // TODO: get pizzas from database and add to pizzas
            }

            double price = Double.parseDouble(names[names.length - 1]);

            return new CustomStrategy(pizzas, price);
        }

    }
}
