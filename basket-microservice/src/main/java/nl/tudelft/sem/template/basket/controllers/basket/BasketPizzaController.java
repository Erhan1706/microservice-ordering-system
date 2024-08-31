package nl.tudelft.sem.template.basket.controllers.basket;

import commons.Ingredient;
import commons.Pizza;
import commons.authentication.AuthenticationManager;
import nl.tudelft.sem.template.basket.builder.PizzaBuilder;
import nl.tudelft.sem.template.basket.models.PizzaRequestModel;
import nl.tudelft.sem.template.basket.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("PMD")
@RestController
@RequestMapping("/api/basket")
public class BasketPizzaController {

    private final PizzaService pizzaService;
    private final BasketService basketService;
    private final IngredientService ingredientService;
    private final transient AuthenticationManager authManager;
    private final PizzaBuilder pizzaBuilder;

    /**
     * Constructor for the basket handler.
     *
     * @param pizzaService  PizzaService instance
     * @param basketService BasketManager instance
     */
    @Autowired
    public BasketPizzaController(PizzaService pizzaService, BasketService basketService,
                            AuthenticationManager authManager, PizzaBuilder pizzaBuilder,
                            IngredientService ingredientService) {
        this.pizzaService = pizzaService;
        this.basketService = basketService;
        this.authManager = authManager;
        this.pizzaBuilder = pizzaBuilder;
        this.ingredientService = ingredientService;
    }

    /**
     * Add a Pizza instance to the basket of the customer if such pizza exists in the menu.
     * Also triggers basket creation if it is the first time that the customer adds a pizza
     * In case the customer does not want a pizza shown in the menu, he also has the option to create a
     * custom one with the available ingredients. This endpoint creates said pizza and adds it to the basket.
     *
     * @param pizzaName the name of the pizza to be saved
     * @return Bad Request or OK, based on whether a pizza with such name exists.
     */
    @PostMapping("/addPizza")
    public ResponseEntity<String> addPizzaToBasket(@RequestBody String pizzaName) {
        String customerId = authManager.getNetId();
        if (basketService.getBasket(customerId) == null) {
            basketService.createBasket(customerId);
        }
        if (pizzaService.findByName(pizzaName) == null) {
            return ResponseEntity.badRequest().body("There is no such pizza as " + pizzaName + " on the menu.");
        } else {
            pizzaBuilder.setName(pizzaName);
            pizzaBuilder.setIngredients(pizzaService.findByName(pizzaName).getIngredients());
            Pizza pizza = pizzaBuilder.build();
            if (pizza == null) {
                return ResponseEntity.badRequest().body("There is no such pizza as " + pizzaName + " on the menu.");
            } else {
                basketService.addPizzaToBasket(customerId, pizza);
                return ResponseEntity.ok("Pizza " + pizzaName + " is added to the basket. Current basket "
                        + "is seen below\n"
                        + basketService.getBasket(customerId).toString());
            }
        }
    }

    /**
     * Removes the pizza from the customer's basket.
     *
     * @param pizzaName the name of the pizza to be removed
     * @return Bad Request or OK, based on whether a pizza with such name exists in the basket
     */
    @DeleteMapping("/removePizza")
    public ResponseEntity<String> removePizzaFromBasket(@RequestBody String pizzaName) {
        String customerId = authManager.getNetId();
        if (basketService.getBasket(customerId).contains(pizzaName)) {
            basketService.removePizzaFromBasket(customerId, pizzaName);
            return ResponseEntity.ok("Pizza " + pizzaName + " is successfully removed from basket.");
        } else {
            return ResponseEntity.badRequest().body("There is no such pizza as " + pizzaName + " in your basket.");
        }
    }

    /**
     * Creates custom pizza and adds to basket.
     * In case the customer does not want a pizza shown in the menu, he also has the option to create a
     * custom one with the available ingredients. This endpoint creates said pizza and adds it to the basket.
     *
     * @param pizzaReqModel the given name and the list of ingredients for the custom pizza
     * @return Bad Request or OK, based on whether a pizza with such name exists.
     */
    @PostMapping("/addPizza/custom")
    public ResponseEntity<String> addCustomPizzaToBasket(@RequestBody PizzaRequestModel pizzaReqModel) {
        String customerId = authManager.getNetId();
        checkBasketExistence(customerId);
        if (pizzaReqModel.getIngredients().isEmpty()) {
            return ResponseEntity.badRequest().body("Please provide at least " + "one ingredient.");
        }
        pizzaBuilder.setName(pizzaReqModel.getName());
        List<Ingredient> ingredients = new ArrayList<>();
        // Check if all ingredients are in the database.
        if (checkIngredientValidity(pizzaReqModel.getIngredients(), ingredients) != null)
            return checkIngredientValidity(pizzaReqModel.getIngredients(), ingredients);
        pizzaBuilder.setIngredients(ingredients);
        Pizza pizza = pizzaBuilder.build();
        basketService.addPizzaToBasket(customerId, pizza);
        return ResponseEntity.ok("Pizza " + pizza.getName() + " is added to the basket. Current basket is seen below:\n"
                + basketService.getBasket(customerId).toString());
    }

    /**
     * Checks to see if there is already a basket for this particular customer.
     *
     * @param customerId the id of the customer
     */
    public void checkBasketExistence(String customerId) {
        if (basketService.getBasket(customerId) == null) {
            basketService.createBasket(customerId);
        }
    }

    /**
     * Checks to see if all the ingredients provided on the request exist on the database.
     *
     * @param reqIngredients the list of ingredients provided in the request
     * @param ingredients a list of ingredients to be later used for the builder
     * @return a bad request response if the repo does not have the ingredients, null otherwise
     */
    public ResponseEntity<String> checkIngredientValidity(List<String> reqIngredients, List<Ingredient> ingredients) {
        for (String ingredient : reqIngredients) {
            if (ingredientService.getByName(ingredient) == null)
                return ResponseEntity.badRequest().body("We do not have "
                        + ingredient + " as an ingredient on our inventory");
            else ingredients.add(ingredientService.getByName(ingredient));
        }
        return null;
    }



}
