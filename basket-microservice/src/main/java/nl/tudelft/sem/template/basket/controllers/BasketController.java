package nl.tudelft.sem.template.basket.controllers;

import commons.Basket;
import commons.Ingredient;
import commons.Pizza;
import commons.authentication.AuthenticationManager;
import commons.Coupon;
import nl.tudelft.sem.template.basket.builder.PizzaBuilder;
import nl.tudelft.sem.template.basket.models.PizzaRequestModel;
import nl.tudelft.sem.template.basket.models.TimeRequestModel;
import nl.tudelft.sem.template.basket.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("PMD")
@RestController
@RequestMapping("/api/basket")
public class BasketController {

    private final PizzaService pizzaService;
    private final CouponService couponService;
    private final BasketService basketService;
    private final IngredientService ingredientService;
    private final transient AuthenticationManager authManager;
    private final PizzaBuilder pizzaBuilder;

    @Autowired
    private RestService restService;

    /**
     * Constructor for the basket handler.
     *
     * @param pizzaService  PizzaService instance
     * @param couponService CouponService instance
     * @param basketService BasketManager instance
     */
    @Autowired
    public BasketController(PizzaService pizzaService, CouponService couponService, BasketService basketService,
                            AuthenticationManager authManager, PizzaBuilder pizzaBuilder,
                            IngredientService ingredientService) {
        this.pizzaService = pizzaService;
        this.couponService = couponService;
        this.basketService = basketService;
        this.authManager = authManager;
        this.pizzaBuilder = pizzaBuilder;
        this.ingredientService = ingredientService;
    }

    /**
     * GET endpoint for the Order MS to retrieve the basket of the customer.
     * Called when the customer decides to checkout
     * Removes the basket from the baskets collection
     *
     * @return the basket of the customer
     */
    @GetMapping("/get")
    public ResponseEntity<Basket> getBasket() {
        String customerId = authManager.getNetId();
        Basket basket = basketService.getBasket(customerId);
        basketService.removeBasket(customerId);
        return ResponseEntity.of(Optional.of(basket));
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
        System.out.println(customerId);
        if (basketService.getBasket(customerId) == null) {
            basketService.createBasket(customerId);
        }
        if (pizzaReqModel.getIngredients().isEmpty()) {
            return ResponseEntity.badRequest().body("Please provide at least " + "one ingredient.");
        }
        pizzaBuilder.setName(pizzaReqModel.getName());
        List<Ingredient> ingredients = new ArrayList<>();
        // Check if all ingredients are in the database.
        for (String ingredient : pizzaReqModel.getIngredients()) {
            if (ingredientService.getByName(ingredient) == null) {
                return ResponseEntity.badRequest().body("We do not have "
                        + ingredient + " as an ingredient on our inventory");
            } else ingredients.add(ingredientService.getByName(ingredient));
        }
        pizzaBuilder.setIngredients(ingredients);
        Pizza pizza = pizzaBuilder.build();
        if (pizza == null) {
            return ResponseEntity.badRequest().body("It is not possible to create this pizza");
        } else {
            basketService.addPizzaToBasket(customerId, pizza);
            return ResponseEntity.ok("Pizza " + pizza.getName()
                    + " is added to the basket. Current basket is seen below: \n"
                    + basketService.getBasket(customerId).toString());
        }
    }

    /**
     * Applies coupon to the basket.
     *
     * @param code the activation code of the coupon to apply
     * @return bad request if
     *              the basket/coupon does not exist,
     *              is already applied, or
     *              coupon has not been applied if the previously applied coupon is cheaper.
     *              ok if else.
     */
    @PostMapping("/applyCoupon")
    public ResponseEntity<String> applyCouponToBasket(@RequestBody String code) {
        String customerId = authManager.getNetId();
        if (basketService.getBasket(customerId) == null) {
            return ResponseEntity.badRequest().body("Your basket is empty!");
        }
        Basket basket = basketService.getBasket(customerId);
        Coupon coupon = couponService.getByCode(code);
        if (coupon == null) {
            return ResponseEntity.badRequest().body("Coupon code: " + code + " is invalid.");
        }
        if (basket.getCoupon() != null && basket.getCoupon().getCode().equalsIgnoreCase(code)) {
            return ResponseEntity.badRequest().body("This coupon is already applied.");
        }


        boolean applied = basketService.applyCouponToBasket(customerId, coupon);
        StringBuilder sb = new StringBuilder();
        DecimalFormat df = new DecimalFormat("0.00");

        if (applied) {
            // If the coupon is applied, the details of the coupon is displayed.
            if (coupon.getType() == 'D') {
                sb.append(coupon.getRate()).append("% discount coupon has been applied.\n");
                sb.append("Current price: €").append(df.format(basket.getPrice()));
            } else if (coupon.getType() == 'F') {
                sb.append("Buy-one-get-one-free coupon has been applied.\n");
                sb.append("Current price: €").append(df.format(basket.getPrice()));
            } else {
                sb.append("Coupon code: ").append(coupon.getCode()).append(" has been applied.\n");
                sb.append("Current price: €").append(df.format(basket.getPrice()));
            }
            return ResponseEntity.ok(sb.toString());
        } else {
            // If the coupon is not applied because the previous coupon is cheaper, it tells that it has not been applied.
            sb.append("Coupon has not been applied because there is a cheaper coupon that has been applied already.");
            return ResponseEntity.badRequest().body(sb.toString());
        }
    }

    /**
     * Removes the coupon that has been applied to the basket.
     *
     * @return bad request if basket has not been created or if there is no coupon applied, ok else.
     */
    @DeleteMapping("/removeCoupon")
    public ResponseEntity<String> removeCouponFromBasket() {
        String customerId = authManager.getNetId();
        Basket basket = basketService.getBasket(customerId);
        if (basket == null || basket.getCoupon() == null) {
            return ResponseEntity.badRequest().body("You do not have any coupon applied in your basket!");
        }
        Coupon coupon = basket.getCoupon();
        basketService.removeCouponFromBasket(customerId);
        StringBuilder sb = new StringBuilder();
        DecimalFormat df = new DecimalFormat("0.00");

        sb.append("Coupon code : ").append(coupon.getCode()).append(" has been removed from your basket.\n");
        sb.append("Current price: €").append(df.format(basket.getPrice()));

        return ResponseEntity.ok(sb.toString());
    }

    /**
     * Displays the overview of the basket.
     * Selects the time when the customer wants to pick their order up.
     *
     * @param timeReqModel hour and minute of the pickup time. Also contains the pickup date (today or tomorrow).
     * @return bad request if
     *              there is no basket created, or
     *              if basket is empty, or
     *              if the selected time is invalid(in the past).
     *              ok else.
     */
    @PostMapping("/selectTime")
    public ResponseEntity<String> selectTime(@RequestBody TimeRequestModel timeReqModel) {
        String customerId = authManager.getNetId();
        Basket basket = basketService.getBasket(customerId);
        if (basket == null || basket.getPizzas().isEmpty()) {
            return ResponseEntity.badRequest().body("Your basket is empty; add items first!");
        }

        LocalDate date = LocalDate.now();
        if (timeReqModel.isTomorrow()) date = date.plusDays(1L);
        LocalTime time = LocalTime.of(timeReqModel.getHour(), timeReqModel.getMinute());

        LocalDateTime pickUpTime = LocalDateTime.of(date, time);
        if (pickUpTime.isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Please enter valid time!");
        }

        basket.setTime(pickUpTime);
        return ResponseEntity.ok("Your selected time: " + date.getMonthValue() + "/" + date.getDayOfMonth() + " "
                + time.getHour() + ":" + time.getMinute());
    }

    /**
     * Displays the overview of the basket
     * Contains list of pizzas (name and price), the coupon that has been applied, and the total price of the basket.
     * If there is no basket with the provided customerId, it tells that the basket is empty.
     *
     * @return the String overview of the basket
     */
    @GetMapping("/overview")
    public ResponseEntity<String> overview() {
        String customerId = authManager.getNetId();
        if (basketService.getBasket(customerId) == null) {
            return ResponseEntity.ok("Your basket is empty!");
        }

        Basket basket = basketService.getBasket(customerId);
        StringBuilder sb = new StringBuilder();
        sb.append("Pizzas:\n");
        if (basket.getPizzas().isEmpty()) sb.append("Nothing is in the basket yet!\n");

        DecimalFormat df = new DecimalFormat("0.00");
        for (Pizza p : basket.getPizzas()) {
            sb.append(p.getName()).append(" | EUR ").append(df.format(p.getPrice())).append('\n');
        }

        sb.append("\nCoupon applied: ");
        Coupon coupon = basket.getCoupon();
        if (coupon == null) {
            sb.append("None");
        } else if (coupon.getType() == 'D') {
            sb.append(coupon.getCode()).append(" (").append(coupon.getRate()).append("% discount coupon)");
        } else if (coupon.getType() == 'F') {
            sb.append(coupon.getCode()).append(" (Buy-one-get-one-free coupon)");
        } else {
            sb.append(coupon.getCode());
        }

        sb.append("\n\nTotal: EUR ").append(df.format(basket.getPrice()));

        sb.append("\n\nYour order will be ready at ").append(basket.timeToString()).append(".");

        return ResponseEntity.ok(sb.toString());
    }

    /**
     * POST endpoint to set the preferred store to be ordered from.
     * Verifies the validity of the input storeID by calling user
     *
     * @param storeId the ID of the preferred store
     * @return Message OK or BAD_Request
     */
    @PostMapping("/setStore")
    public ResponseEntity<String> setStorePreference(@RequestBody int storeId) {
        if (!restService.verifyStoreId(storeId)) {
            return ResponseEntity.badRequest().body("Invalid storeID. Please try again.");
        } else {
            return ResponseEntity.ok(basketService.setStorePreference(authManager.getNetId(), storeId));
        }
    }
}
