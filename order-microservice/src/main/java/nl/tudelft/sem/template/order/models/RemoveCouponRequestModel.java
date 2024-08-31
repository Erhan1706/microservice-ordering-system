package nl.tudelft.sem.template.order.models;

import commons.Coupon;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RemoveCouponRequestModel {
    private String code;
    private char type;
    private double rate;
    private boolean limitedTime;

    public RemoveCouponRequestModel() {}

    public RemoveCouponRequestModel(Coupon coupon) {
        if (coupon == null) return;
        code = coupon.getCode();
        rate = coupon.getRate();
        type = coupon.getType();
        limitedTime = coupon.isLimitedTime();
    }
}
