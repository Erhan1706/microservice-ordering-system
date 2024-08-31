package nl.tudelft.sem.template.basket.controllers.repo;

import commons.Ingredient;
import commons.authentication.AuthenticationManager;
import nl.tudelft.sem.template.basket.models.IngredientRequestModel;
import nl.tudelft.sem.template.basket.services.IngredientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Ingredient DB controller is responsible for any incoming requests regarding the Ingredient DB.
 * Includes:
 * - GETting all ingredients from the repo
 * - ADDing a new ingredient into the repo
 * - GETting all ingredients in a list display for allergy selection
 */
@SuppressWarnings("PMD")
@RestController
@RequestMapping("/api/repo/ingredients")
public class IngredientRepoController {

    private final IngredientService ingredientService;
    private final transient AuthenticationManager authManager;

    /**
     * Constructor for the ingredient repo controller.
     *
     * @param ingredientService IngredientService instance
     * @param authManager       AuthenticationManager instance
     */
    @Autowired
    public IngredientRepoController(IngredientService ingredientService, AuthenticationManager authManager) {
        this.ingredientService = ingredientService;
        this.authManager = authManager;
    }

    /**
     * Getter for ingredients.
     *
     * @return all ingredients on the database
     */
    @GetMapping("")
    public ResponseEntity<List<Ingredient>> getIngredients() {
        return new ResponseEntity<>(ingredientService.findAll(), HttpStatus.OK);
    }

    /**
     * Adds the passed ingredient to the ingredient repository.
     *
     * @param ingredientRm ingredient request model, same fields, except for ID
     * @return OK or BAD REQUEST, with description
     */
    @PostMapping("/add")
    public ResponseEntity<String> addIngredientToRepo(@RequestBody IngredientRequestModel ingredientRm) {
        // only stores and managers are allowed to add ingredients to the database
        if (authManager.getRole().equals("customer")) {
            return ResponseEntity.badRequest().body("Only stores and managers can add ingredients to the database!");
        }
        if (!ingredientRm.getName().isEmpty() && ingredientRm.getPrice() > 0.0
                && !ingredientService.exists(ingredientRm.getName())) {
            Ingredient ingredient = new Ingredient(ingredientRm.getName(), ingredientRm.getPrice());

            return ResponseEntity.ok(ingredientService.save(ingredient).getName() + " is added to the repository.");
        } else {
            return ResponseEntity.badRequest().body("This ingredient is invalid or already exists.");
        }
    }

    /**
     * Getter for the list of ingredients that a customer can choose from.
     *
     * @return all ingredients in the form: {id} - {name}
     */
    @GetMapping("/allergies")
    public ResponseEntity<List<String>> getAllergies() {

        List<Ingredient> ingredientList = ingredientService.findAll();
        List<String> allAllergies = new ArrayList<>();

        for (Ingredient i : ingredientList) {
            allAllergies.add(i.getId().toString() + " - " + i.getName());
        }

        return new ResponseEntity<>(allAllergies, HttpStatus.OK);
    }
}
