package nl.tudelft.sem.template.order.services;

import nl.tudelft.sem.template.order.models.RemoveCouponRequestModel;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import nl.tudelft.sem.template.order.models.AddOrderRequestModel;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;

@SuppressWarnings("PMD")
@Service
public class RestService {

    private final RestTemplate restTemplate;

    @Autowired
    public RestService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public ResponseEntity<String> removeCoupon(RemoveCouponRequestModel coupon, String token) {
        if (!coupon.isLimitedTime()) return ResponseEntity.ok("Success!");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> request = new HttpEntity<>(coupon.getCode(), headers);

        String url = "http://localhost:8083/api/repo/coupons/delete";
        try {
            return this.restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error occured while trying to delete coupon.\n" + e.getMessage());
        }
    }


    public ResponseEntity<AddOrderRequestModel> getBasket(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> request = new HttpEntity<>(headers);
        String url = "http://localhost:8083/api/basket/get";
        try {
            return this.restTemplate.exchange(url, HttpMethod.GET, request, AddOrderRequestModel.class);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AddOrderRequestModel());
        }
    }
}
