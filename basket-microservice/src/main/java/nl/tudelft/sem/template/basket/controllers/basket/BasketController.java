package nl.tudelft.sem.template.basket.controllers.basket;

import commons.Basket;
import commons.Pizza;
import commons.authentication.AuthenticationManager;
import commons.Coupon;
import nl.tudelft.sem.template.basket.models.TimeRequestModel;
import nl.tudelft.sem.template.basket.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@SuppressWarnings("PMD")
@RestController
@RequestMapping("/api/basket")
public class BasketController {

    private final BasketService basketService;
    private final transient AuthenticationManager authManager;

    @Autowired
    private RestService restService;

    /**
     * Constructor for the basket handler.
     *
     * @param basketService BasketManager instance
     */
    @Autowired
    public BasketController(BasketService basketService, AuthenticationManager authManager) {
        this.basketService = basketService;
        this.authManager = authManager;
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
        if (basket == null || basket.getBasketInfo().getPizzas().isEmpty()) {
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
        if (basket.getBasketInfo().getPizzas().isEmpty()) sb.append("Nothing is in the basket yet!\n");
        sb.append(basketCalculatePrice(basket));
        sb.append("\nCoupon applied: ");
        sb.append(printCoupon(basket.getBasketInfo().getCoupon()));
        sb.append("\n\nYour order will be ready at ").append(basket.timeToString()).append(".");

        return ResponseEntity.ok(sb.toString());
    }

    /**
     * Prints the basket price.
     *
     * @param basket basket to be printed
     * @return the string representation of the basket price
     */
    public String basketCalculatePrice(Basket basket) {
        StringBuilder sb = new StringBuilder();
        DecimalFormat df = new DecimalFormat("0.00");
        for (Pizza p : basket.getBasketInfo().getPizzas()) {
            sb.append(p.getName()).append(" | EUR ").append(df.format(p.getPrice())).append('\n');
        }

        sb.append("\n\nTotal: EUR ").append(df.format(basket.getBasketInfo().getPrice()));
        return sb.toString();
    }

    /**
     * Prints the coupon.
     *
     * @param coupon - the coupon that will be printed
     * @return the string representation of the coupon
     */
    public String printCoupon(Coupon coupon) {
        StringBuilder sb = new StringBuilder();
        if (coupon == null) {
            sb.append("None");
        } else if (coupon.getType() == 'D') {
            sb.append(coupon.getCode()).append(" (").append(coupon.getRate()).append("% discount coupon)");
        } else if (coupon.getType() == 'F') {
            sb.append(coupon.getCode()).append(" (Buy-one-get-one-free coupon)");
        } else {
            sb.append(coupon.getCode());
        }
        return sb.toString();
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
