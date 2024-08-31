package commons;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("PMD")
@Entity
@Table
public class Pizza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "name")
    String name;

    @ManyToMany
    @JoinTable(
            name = "pizza_ingredient",
            joinColumns = @JoinColumn(name = "pizza_id"),
            inverseJoinColumns = @JoinColumn(name = "ingredient_id")
    )
    private List<Ingredient> ingredients;

    @Column(name = "price")
    private Double price;

    public Pizza() {
    }

    public Pizza(String name, List<Ingredient> ingredients) {
        this.name = name;
        this.ingredients = ingredients;
        this.price = Math.round(calculatePrice(ingredients) * 100.0) / 100.0; // Round to 2 decimal places
    }

    /**
     * Method calculating the price of the pizza based on its ingredients + profit margin.
     *
     * @param ingredients the list of ingredients of the pizza
     * @return the price of the pizza
     */
    public Double calculatePrice(List<Ingredient> ingredients) {
        // Because the franchise needs to make profit
        Double totalPrice = 3.00;
        for (Ingredient ingredient : ingredients) {
            totalPrice += ingredient.getPrice();
        }
        return totalPrice;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Pizza pizza = (Pizza) o;

        if (id != null ? !id.equals(pizza.id) : pizza.id != null) {
            return false;
        }
        if (name != null ? !name.equals(pizza.name) : pizza.name != null) {
            return false;
        }
        if (ingredients != null ? !ingredients.equals(pizza.ingredients) :
            pizza.ingredients != null) {
            return false;
        }
        return price != null ? price.equals(pizza.price) : pizza.price == null;
    }


    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, ingredients, price);
    }
}
