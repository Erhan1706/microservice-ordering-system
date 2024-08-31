package nl.tudelft.sem.template.basket.controllers.repo;


import commons.Ingredient;
import commons.Pizza;
import commons.authentication.AuthenticationManager;
import nl.tudelft.sem.template.basket.AllergiesResponseModel;
import nl.tudelft.sem.template.basket.models.PizzaRequestModel;
import nl.tudelft.sem.template.basket.services.IngredientService;
import nl.tudelft.sem.template.basket.services.PizzaService;
import nl.tudelft.sem.template.basket.services.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Pizza DB controller is responsible for any incoming requests regarding the Pizza DB.
 * Includes:
 * - GETting all pizzas from the repo (+ filter out)
 * - ADDing a new pizza to the repo
 */
@SuppressWarnings("PMD")
@RestController
@RequestMapping("/api/repo/pizzas")
public class PizzaRepoController {

    private final PizzaService pizzaService;
    private final IngredientService ingredientService;
    private final transient AuthenticationManager authManager;
    private final RestService restService;

    /**
     * Constructor for the pizza repo controller.
     *
     * @param pizzaService      PizzaService instance
     * @param ingredientService IngredientService instance
     * @param authManager       AuthManager instance
     * @param restService       RestService instance
     */
    @Autowired
    public PizzaRepoController(PizzaService pizzaService, IngredientService ingredientService,
                               AuthenticationManager authManager, RestService restService) {
        this.pizzaService = pizzaService;
        this.ingredientService = ingredientService;
        this.authManager = authManager;
        this.restService = restService;
    }

    /**
     * Returns all pizzas on the menu.
     * Filters out pizzas containing allergens by customer's request.
     *
     * @param filterOut Boolean for whether the customer wants pizzas to be filtered
     * @param token     Token of the customer, to retrieve allergens
     * @return the menu of pizzas to display
     */
    @GetMapping("/{filterOut}")
    public ResponseEntity<List<Pizza>> getPizzas(@PathVariable("filterOut") Boolean filterOut,
                                                 @RequestHeader(name = "Authorization") String token) {
        AllergiesResponseModel request = restService.getAllergiesNetId(token).getBody();
        List<Long> allergies = request.getAllergies();
        List<Pizza> allPizzas = pizzaService.findAll();

        if (filterOut) {
            allergies.remove(0L);
            List<Ingredient> allergens = new ArrayList<>();
            for (Long l : allergies) {
                allergens.add(ingredientService.findById(l));
            }
            allPizzas = pizzaService.filterOutPizzas(allergens, allPizzas);
        }
        return new ResponseEntity<>(allPizzas, HttpStatus.OK);
    }


    /**
     * Add a Pizza instance to the pizza repo if the pizza is valid, and if all ingredients exist.
     * PizzaReqModel's ingredient list contains the names of the ingredients.
     *
     * @param pizzaRm the pizza to be saved
     * @return Bad Request or OK, based on validity of the pizza
     */
    @PostMapping("/addToRepo")
    public ResponseEntity<String> addPizzaToRepo(@RequestBody PizzaRequestModel pizzaRm) {
        // only stores and managers are allowed to add pizzas to the database
        if (authManager.getRole().equals("customer")) {
            return ResponseEntity.badRequest().body("Only stores and managers can add new pizzas to the database!");
        }
        List<Ingredient> ingredients = new ArrayList<>();
        for (String ingredientName : pizzaRm.getIngredients()) {
            Ingredient found = ingredientService.getByName(ingredientName);
            if (found == null) {
                return ResponseEntity.badRequest().body("Ingredient " + ingredientName + " does not exist.");
            } else {
                ingredients.add(ingredientService.getByName(ingredientName));
            }
        }
        Pizza pizza = new Pizza(pizzaRm.getName(), ingredients);
        if (pizzaService.invalid(pizza)) {
            return ResponseEntity.badRequest().body("This pizza is invalid or already exists.");
        } else {
            pizzaService.save(pizza);
            return ResponseEntity.ok(pizza.getName() + " is added to the repository.");
        }
    }

}
