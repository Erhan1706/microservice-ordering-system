package nl.tudelft.sem.template.basket.config;

import commons.Coupon;
import commons.Pizza;
import commons.strategies.*;
import nl.tudelft.sem.template.basket.repositories.CouponRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class CouponConfig {
    @Bean
    CommandLineRunner commandLineRunner2(CouponRepository repo) {
        return args -> {
            Coupon discount = new Coupon("EXPL01");
            repo.save(discount);

            Coupon getOneFree = new Coupon("EXPL02", 'F');
            repo.save(getOneFree);

            Coupon timeOnly = new Coupon("EXPL03", true);
            repo.save(timeOnly);

            Coupon typeTime = new Coupon("EXPL04", 'F', true);
            repo.save(typeTime);

            Coupon eventSample = new Coupon("EXPL05", 'D', 25, false);
            repo.save(eventSample);
        };
    }
}

