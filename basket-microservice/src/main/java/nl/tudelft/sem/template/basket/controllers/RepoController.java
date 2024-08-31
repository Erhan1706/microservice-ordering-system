package nl.tudelft.sem.template.basket.controllers;

import commons.Coupon;
import commons.Ingredient;
import commons.Pizza;
import commons.authentication.AuthenticationManager;
import nl.tudelft.sem.template.basket.AllergiesResponseModel;
import nl.tudelft.sem.template.basket.services.RestService;
import nl.tudelft.sem.template.basket.models.CouponRequestModel;
import nl.tudelft.sem.template.basket.models.IngredientRequestModel;
import nl.tudelft.sem.template.basket.models.PizzaRequestModel;
import nl.tudelft.sem.template.basket.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Basket controller is responsible for any incoming requests to the Baskets Microservice.
 * Includes:
 * - GETting all pizzas and ingredients from their repos
 * - ADDing new pizzas and ingredients into the repos
 */
@SuppressWarnings("PMD")
@RestController
@RequestMapping("/api/repo")
public class RepoController {


    private final RestService restService;
    private final PizzaService pizzaService;
    private final CouponService couponService;
    private final IngredientService ingredientService;
    private final transient AuthenticationManager authManager;


    /**
     * Constructor for the basket controller.
     *
     * @param pizzaService      PizzaService instance
     * @param ingredientService IngredientService instance
     */
    @Autowired
    public RepoController(PizzaService pizzaService, CouponService couponService,
                          IngredientService ingredientService, AuthenticationManager authManager,
                          RestService restService) {
        this.pizzaService = pizzaService;
        this.couponService = couponService;
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
    @GetMapping("/pizzas/{filterOut}")
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
    @PostMapping("/pizzas/addToRepo")
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

    /**
     * Adds the passed ingredient to the ingredient repository.
     *
     * @param ingredientRm ingredient request model, same fields, except for ID
     * @return OK or BAD REQUEST, with description
     */
    @PostMapping("/ingredients/add")
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
     * Getter for ingredients.
     *
     * @return all ingredients on the database
     */
    @GetMapping("/ingredients")
    public ResponseEntity<List<Ingredient>> getIngredients() {
        return new ResponseEntity<>(ingredientService.findAll(), HttpStatus.OK);
    }

    /**
     * Getter for the list of ingredients that a customer can choose from.
     *
     * @return all ingredients in the form: {id} - {name}
     */
    @GetMapping("/ingredients/allergies")
    public ResponseEntity<List<String>> getAllergies() {

        List<Ingredient> ingredientList = ingredientService.findAll();
        List<String> allAllergies = new ArrayList<>();

        for (Ingredient i : ingredientList) {
            allAllergies.add(i.getId().toString() + " - " + i.getName());
        }

        return new ResponseEntity<>(allAllergies, HttpStatus.OK);
    }

    // ------------------------------------------------------------------------------
    // From here are methods and endpoints related to coupons.
    // ------------------------------------------------------------------------------

    /**
     * Get all the coupons in the repository.
     *
     * @return List of all coupons in the repository
     */
    @GetMapping("/coupons")
    public ResponseEntity<List<Coupon>> getAllCoupons() {
        return new ResponseEntity<>(couponService.findAll(), HttpStatus.OK);
    }

    /**
     * Add a coupon to the repository.
     *
     * @param couponRm the coupon to be saved
     * @return Bad request if coupon activation code is invalid or already exists, OK if valid.
     */
    @PostMapping("/coupons/addToRepo")
    public ResponseEntity<String> addCouponToRepo(@RequestBody CouponRequestModel couponRm) {
        // only stores and managers are allowed to add coupons to the database
        if (authManager.getRole().equals("customer")) {
            return ResponseEntity.badRequest().body("Only stores and managers can add new coupons to the database!");
        }
        Coupon coupon = new Coupon(couponRm.getCode(), couponRm.getType(), couponRm.getRate(),
                couponRm.isLimitedTime());

        if (couponService.couponInvalid(coupon)) {
            return ResponseEntity.badRequest().body(
                    "The coupon code must be formatted with 4 characters followed by 2 numbers.");
        } else if (couponService.exists(coupon.getCode())) {
            return ResponseEntity.badRequest().body("Coupon with the provided activation code already exists.");
        } else {
            couponService.save(coupon);
            return ResponseEntity.ok("Coupon code: " + coupon.getCode() + " is added to the repository.");
        }
    }

    /**
     * Deletes a coupon in the database.
     *
     * @param code the activation code of the coupon to be deleted.
     * @return bad request if the coupon with the provided details do not exist, ok if else.
     */
    @DeleteMapping("/coupons/delete")
    public ResponseEntity<String> deleteCoupon(@RequestBody String code) {
        // only stores and managers are allowed to delete coupons from the database
        if (authManager.getRole().equals("customer")) {
            return ResponseEntity.badRequest().body("Only stores and managers can delete coupons from the database!");
        }

        Coupon coupon = couponService.getByCode(code);

        if (coupon == null) {
            return ResponseEntity.badRequest().body("Coupon code: " + code + " does not exist.");
        } else {
            couponService.delete(coupon);
            return ResponseEntity.ok("Coupon code: " + code + " has been deleted.");
        }
    }

    /**
     * Get a specific coupon in the repository.
     *
     * @param code the activation code of the coupon to search for
     * @return the details of the requested coupon (null if coupon does not exist)
     */
    @GetMapping("/coupons/getCoupon")
    public ResponseEntity<Coupon> getCoupon(@RequestBody String code) {
        Coupon coupon = couponService.getByCode(code);

        if (coupon == null) return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        else return new ResponseEntity<>(coupon, HttpStatus.OK);
    }
}
