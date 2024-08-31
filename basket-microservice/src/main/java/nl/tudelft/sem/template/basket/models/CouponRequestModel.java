package nl.tudelft.sem.template.basket.models;

import commons.Coupon;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CouponRequestModel {

    private String code;
    private char type;
    private double rate;
    private boolean limitedTime;


    public CouponRequestModel() {}

    public CouponRequestModel(Coupon coupon) {
        code = coupon.getCode();
        type = coupon.getType();
        rate = coupon.getRate();
        limitedTime = coupon.isLimitedTime();
    }
}
