package nl.tudelft.sem.template.basket.services;

import java.util.ArrayList;
import java.util.List;

import nl.tudelft.sem.template.basket.AllergiesResponseModel;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.client.RestTemplate;

@Service
@SuppressWarnings("PMD")
public class RestService {

    private final RestTemplate restTemplate;

    public RestService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    /**
    * Makes a request to User MS's verify GET endpoint.
    * To verify the customer input storeID
    *
    * @param storeId the customer input prefered store' ID
    * @return TRUE if verified, and vice-versa
    */
    public Boolean verifyStoreId(int storeId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Object> request = new HttpEntity<>(headers);
        String url = "http://localhost:8081/verify/" + storeId;
        try {
            return this.restTemplate.exchange(url, HttpMethod.GET, request, Boolean.class).getBody();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(false).getBody();
        }
    }

    public ResponseEntity<AllergiesResponseModel> getAllergiesNetId(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token.substring(7));
        HttpEntity<String> request = new HttpEntity<>(headers);
        String url = "http://localhost:8081/getAllergies";
        try {
            return this.restTemplate.exchange(url, HttpMethod.GET, request, AllergiesResponseModel.class);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AllergiesResponseModel());
        }
    }
}
