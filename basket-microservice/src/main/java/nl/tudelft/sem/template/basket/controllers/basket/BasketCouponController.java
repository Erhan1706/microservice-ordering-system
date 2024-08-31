package nl.tudelft.sem.template.basket.controllers.basket;

import commons.Basket;
import commons.authentication.AuthenticationManager;
import commons.Coupon;
import nl.tudelft.sem.template.basket.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.DecimalFormat;

@SuppressWarnings("PMD")
@RestController
@RequestMapping("/api/basket")
public class BasketCouponController {

    private final CouponService couponService;
    private final BasketService basketService;
    private final transient AuthenticationManager authManager;


    /**
     * Constructor for the basket handler.
     *
     * @param couponService CouponService instance
     * @param basketService BasketManager instance
     */
    @Autowired
    public BasketCouponController(CouponService couponService, BasketService basketService,
                            AuthenticationManager authManager) {
        this.couponService = couponService;
        this.basketService = basketService;
        this.authManager = authManager;
    }

    /**
     * Applies coupon to the basket.
     *
     * @param code the activation code of the coupon to apply
     * @return ok if coupon has been applied successfully, bad_request otherwise. */
    @PostMapping("/applyCoupon")
    public ResponseEntity<String> applyCouponToBasket(@RequestBody String code) {
        if (basketService.getBasket(authManager.getNetId()) == null)
            return ResponseEntity.badRequest().body("Your basket is empty!");

        return couponChecker(authManager.getNetId(), code);
    }

    public ResponseEntity<String> couponChecker(String customerId, String code) {
        Basket basket = basketService.getBasket(customerId);

        if (couponService.getByCode(code) == null) {
            return ResponseEntity.badRequest().body("Coupon code: " + code + " is invalid.");
        }
        if (basket.getBasketInfo().getCoupon() != null
                && basket.getBasketInfo().getCoupon().getCode().equalsIgnoreCase(code)) {
            return ResponseEntity.badRequest().body("This coupon is already applied.");
        }

        return couponApplier(customerId, couponService.getByCode(code));
    }

    public ResponseEntity<String> couponApplier(String customerId, Coupon coupon) {
        boolean applied = basketService.applyCouponToBasket(customerId, coupon);
        if (!applied) return ResponseEntity.badRequest().body(
                "Coupon has not been applied because there is a cheaper coupon that has been applied already.");

        Basket basket = basketService.getBasket(customerId);
        DecimalFormat df = new DecimalFormat("0.00");
        return ResponseEntity.ok(coupon.getMessage()
                + "Current price: €" + df.format(basket.getBasketInfo().getPrice()));
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
        if (basket == null || basket.getBasketInfo().getCoupon() == null) {
            return ResponseEntity.badRequest().body("You do not have any coupon applied in your basket!");
        }
        Coupon coupon = basket.getBasketInfo().getCoupon();
        basketService.removeCouponFromBasket(customerId);
        StringBuilder sb = new StringBuilder();
        DecimalFormat df = new DecimalFormat("0.00");

        sb.append("Coupon code : ").append(coupon.getCode()).append(" has been removed from your basket.\n");
        sb.append("Current price: €").append(df.format(basket.getBasketInfo().getPrice()));

        return ResponseEntity.ok(sb.toString());
    }

}
