package nl.tudelft.sem.template.basket.services;

import commons.Ingredient;
import nl.tudelft.sem.template.basket.repositories.IngredientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@SuppressWarnings("PMD")
@Service
public class IngredientService {

    private final IngredientRepository ingredientRepository;

    @Autowired
    public IngredientService(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    public List<Ingredient> findAll() {
        return ingredientRepository.findAll();
    }

    public Ingredient getByName(String name) {
        return ingredientRepository.getIngredientByName(name).orElse(null);
    }

    public Ingredient findById(Long id)  {
        return ingredientRepository.findById(id).orElseGet(null);
    }

    public Ingredient save(Ingredient ingredient) {
        return ingredientRepository.save(ingredient);
    }

    /**
     * Verifies whether there is such ingredient with identical name.
     *
     * @param ingredientName the ingredient name to be checked
     * @return True if there is an ingredient in the repo with the same name, False otherwise
     */
    public Boolean exists(String ingredientName) {
        for (Ingredient ingredient : ingredientRepository.findAll()) {
            if (ingredient.getName().equals(ingredientName))
                return true;
        }
        return false;
    }
}
