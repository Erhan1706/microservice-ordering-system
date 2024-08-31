package nl.tudelft.sem.template.basket.controllers.repo;

import commons.Coupon;
import commons.authentication.AuthenticationManager;
import nl.tudelft.sem.template.basket.models.CouponRequestModel;
import nl.tudelft.sem.template.basket.services.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Coupon DB controller is responsible for any incoming requests regarding the Coupon DB.
 * Includes:
 * - GETting all coupons  from the repo
 * - GETting a specific coupon by its code
 * - ADDing a new coupon to the repo
 * - REMOVEing a coupon from the repo
 */
@SuppressWarnings("PMD")
@RestController
@RequestMapping("/api/repo/coupons")
public class CouponRepoController {

    private final CouponService couponService;
    private final transient AuthenticationManager authManager;


    /**
     * Constructor for the coupon repo controller.
     *
     * @param couponService CouponService instance
     * @param authManager   AuthenticationManager instance
     */
    @Autowired
    public CouponRepoController(CouponService couponService, AuthenticationManager authManager) {
        this.couponService = couponService;
        this.authManager = authManager;
    }

    /**
     * Get all the coupons in the repository.
     *
     * @return List of all coupons in the repository
     */
    @GetMapping("")
    public ResponseEntity<List<Coupon>> getAllCoupons() {
        return new ResponseEntity<>(couponService.findAll(), HttpStatus.OK);
    }

    /**
     * Get a specific coupon in the repository.
     *
     * @param code the activation code of the coupon to search for
     * @return the details of the requested coupon (null if coupon does not exist)
     */
    @GetMapping("/getCoupon")
    public ResponseEntity<Coupon> getCoupon(@RequestBody String code) {
        Coupon coupon = couponService.getByCode(code);

        if (coupon == null) return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        else return new ResponseEntity<>(coupon, HttpStatus.OK);
    }

    /**
     * Add a coupon to the repository.
     *
     * @param couponRm the coupon to be saved
     * @return Bad request if coupon activation code is invalid or already exists, OK if valid.
     */
    @PostMapping("/addToRepo")
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
    @DeleteMapping("/delete")
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
}
