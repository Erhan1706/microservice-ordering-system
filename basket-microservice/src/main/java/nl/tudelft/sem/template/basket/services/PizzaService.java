package nl.tudelft.sem.template.basket.services;

import commons.Pizza;
import commons.Ingredient;
import java.util.ArrayList;
import nl.tudelft.sem.template.basket.repositories.PizzaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@SuppressWarnings("PMD")
@Service
public class PizzaService {

    private final PizzaRepository pizzaRepository;

    @Autowired
    public PizzaService(PizzaRepository pizzaRepository) {
        this.pizzaRepository = pizzaRepository;
    }

    public List<Pizza> findAll() {
        return pizzaRepository.findAll();
    }

    public Pizza save(Pizza pizza) {
        return pizzaRepository.save(pizza);
    }

    /**
     * Passed a pizza, checks whether an identical pizza is already in the repository.
     * Two pizzas are identical if they have the same name or the same ingredient list.
     *
     * @param pizza The pizza to be checked for the duplicate
     * @return True if there is an identical pizza in the repo, and False else
     */
    public Boolean exists(Pizza pizza) {
        List<Pizza> all = findAll();
        for (Pizza pizzaSelected : all) {
            if (Objects.equals(pizzaSelected.getName(), pizza.getName())
                    || Objects.equals(pizzaSelected.getIngredients(), pizza.getIngredients())) return true;
        }
        return false;
    }

    /**
     * Checker for pizza validity prior to adding to the repo.
     * If pizza already exists in the repo, is null, has no name or no ingredients, returns true (is invalid).
     *
     * @param pizza to be checked for validity.
     * @return true if the pizza is invalid, false else.
     */
    public Boolean invalid(Pizza pizza) {
        return exists(pizza) || pizza == null || pizza.getName().isEmpty() || pizza.getIngredients().isEmpty();
    }

    /**
     * Accessory method that filters out all pizzas containing allergens from the input list.
     *
     * @param allergens the list of allergens of the customer
     * @param allPizzas all pizzas on the menu
     */
    public List<Pizza> filterOutPizzas(List<Ingredient> allergens, List<Pizza> allPizzas) {
        List<Pizza> res = new ArrayList<>();
        for (Pizza pizza : allPizzas) {
            for (Ingredient ingredient : allergens) {
                if (!pizza.getIngredients().contains(ingredient)) {
                    res.add(pizza);
                }
            }
        }
        return res;
    }

    public Pizza findByName(String pizzaName) {
        Pizza result = null;
        for (Pizza pizza : pizzaRepository.findAll()) {
            if (pizza.getName().equals(pizzaName)) {
                result = pizza;
            }
        }
        return result;
    }
}
