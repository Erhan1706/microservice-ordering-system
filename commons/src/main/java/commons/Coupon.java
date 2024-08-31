package commons;

import commons.strategies.*;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("PMD")
@Entity
@Table(name = "COUPON")
public class Coupon {

    /**
     * ID of the coupon.
     * Key attribute and is generated automatically in creation.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "COUPON_ID")
    long id;

    /**
     * Activation code of the coupon.
     * This is used to search the coupon in the database.
     */
    @Column(name = "COUPON_CODE")
    String code;

    /**
     * Type of the coupon.
     * 'D' = discount coupon
     * 'F' = buy-one-get-one-free
     * 'C' = custom coupon
     */
    @Column(name = "COUPON_TYPE")
    char type;

    /**
     * If this is a discount coupon, the discount rate is stored in this attribute (in percentage)
     * Otherwise this value is 0.
     */
    @Column(name = "COUPON_RATE")
    double rate;

    /**
     * Shows whether this coupon can be used multiple times
     * If this field is true, the coupon is not deleted after use.
     * If this field is false, the coupon must be deleted after use.
     */
    @Column(name = "COUPON_LIMITED")
    boolean limitedTime;

    /**
     * This is the strategy to calculate the new price.
     * Could be seen as equivalent to type of the coupon.
     * If this field is not specified in the constructor, 30% discount strategy is used.
     * When storing in the database, this field is stored as String.
     */
    @Column(name = "COUPON_STRATEGY")
    @Convert(converter = StrategyConverter.class)
    PriceStrategy strategy;

    public Coupon(){}

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Coupon coupon = (Coupon) o;

        if (id != coupon.id) {
            return false;
        }
        if (type != coupon.type) {
            return false;
        }
        if (Double.compare(coupon.rate, rate) != 0) {
            return false;
        }
        if (limitedTime != coupon.limitedTime) {
            return false;
        }
        if (code != null ? !code.equals(coupon.code) : coupon.code != null) {
            return false;
        }
        return strategy != null ? strategy.equals(coupon.strategy) : coupon.strategy == null;
    }

    /**
     * The default constructor of Coupon.
     *
     * @param code the activation code of the coupon.
     * @param type the type of the coupon (D if discount, F if buy-one-get-one-free).
     * @param rate the discount rate of the coupon (only applicable for discount coupons; 0 for other types).
     * @param limitedTime true if this coupon can be used multiple times, false otherwise.
     */
    public Coupon(String code, char type, double rate, boolean limitedTime) {
        this.code = code;
        this.type = type;
        this.limitedTime = limitedTime;

        if (type == 'D') {
            this.rate = rate;
            this.strategy = new DiscountStrategy(rate);
        } else if (type == 'F') {
            this.rate = 0;
            this.strategy = new FreeStrategy();
        } else {
            this.rate = 0;
            // TODO: create custom coupon
        }
    }

    /**
     * When only activation code is passed on as the argument,
     * a default coupon is created which is a 30% discount coupon that expires after use.
     *
     * @param code the activation code of the coupon.
     */
    public Coupon(String code) {
        this.code = code;
        this.type = 'D';
        this.rate = 30;
        this.limitedTime = false;
        this.strategy = new DiscountStrategy(30);
    }

    /**
     * Creates a coupon of given type.
     * If the type is discount ('D'), the rate is set to 30 by default.
     *
     * @param code the activation code of the coupon.
     * @param type the type of the coupon.
     */
    public Coupon(String code, char type) {
        this.code = code;
        this.type = type;
        this.limitedTime = false;

        if (type == 'D') {
            this.rate = 30;
            this.strategy = new DiscountStrategy(30);
        } else if (type == 'F') {
            this.rate = 0;
            this.strategy = new FreeStrategy();
        } else {
            this.rate = 0;
            this.strategy = new CustomStrategy(new ArrayList<>(), 0);
        }
    }

    /**
     * Creates a default coupon (30% discount coupon)
     * the availability of the coupon (if this is only for one use) can be determined by limitedTime value.
     *
     * @param code the activation code of the coupon.
     * @param limitedTime true if this coupon can be used multiple times, false otherwise.
     */
    public Coupon(String code, boolean limitedTime) {
        this.code = code;
        this.type = 'D';
        this.rate = 30;
        this.limitedTime = limitedTime;
        this.strategy = new DiscountStrategy(30);
    }

    /**
     * Creates a coupon with a given type.
     * If the type provided is discount('D'), a 30% discount coupon is created by default.
     * the availability of the coupon can also be determined.
     *
     * @param code the activation code of the coupon.
     * @param type the type of the coupon.
     * @param limitedTime true if this coupon can be used multiple times, false otherwise.
     */
    public Coupon(String code, char type, boolean limitedTime) {
        this.code = code;
        this.type = type;
        this.limitedTime = limitedTime;

        if (type == 'D') {
            this.rate = 30;
            this.strategy = new DiscountStrategy(30);
        } else if (type == 'F') {
            this.rate = 0;
            this.strategy = new FreeStrategy();
        } else {
            this.rate = 0;
            // TODO: create custom coupon
            this.strategy = new CustomStrategy(new ArrayList<>(), 0);
        }
    }

    public long getId() {
        return id;
    }

    public String getCode() { return code; }

    public char getType() { return this.type; }

    public double getRate() { return this.rate; }

    public boolean isLimitedTime() { return limitedTime; }

    public double calculatePrice(List<Pizza> pizzas) { return this.strategy.calculatePrice(pizzas); }

    public String getMessage() { return this.strategy.getMessage(); }

}