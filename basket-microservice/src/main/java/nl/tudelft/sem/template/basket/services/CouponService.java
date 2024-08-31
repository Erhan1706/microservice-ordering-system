package nl.tudelft.sem.template.basket.services;

import commons.Coupon;
import nl.tudelft.sem.template.basket.repositories.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@SuppressWarnings("PMD")
@Service
public class CouponService {

    private final CouponRepository couponRepo;

    @Autowired
    public CouponService(CouponRepository c) {
        this.couponRepo = c;
    }

    public List<Coupon> findAll() {
        return couponRepo.findAll();
    }

    public Coupon getByCode(String code) {
        return couponRepo.findCouponByCodeIgnoreCase(code).orElse(null);
    }

    public Coupon save(Coupon c) {
        return couponRepo.save(c);
    }

    public Coupon delete(Coupon c) {
        couponRepo.delete(c);
        return c;
    }

    public boolean exists(String code) {
        List<Coupon> coupons = findAll();
        for (Coupon c : coupons) {
            if (c.getCode().equals(code)) return true;
        }
        return false;
    }

    /**
     * Checks if the activation code of the coupon follows the format (4 characters, 2 numbers).
     *
     * @param c the coupon to check
     * @return true if coupon does not follow the format, false else
     */
    public boolean couponInvalid(Coupon c) {
        return !c.getCode().matches("(?i)^[A-Z]{4}[0-9]{2}$");
    }
}