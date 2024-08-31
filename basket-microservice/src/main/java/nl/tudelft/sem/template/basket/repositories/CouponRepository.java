package nl.tudelft.sem.template.basket.repositories;

import commons.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    @Query
    Optional<Coupon> findCouponByCodeIgnoreCase(@Param("code") String code);
}