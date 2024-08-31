package nl.tudelft.sem.template.basket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@EntityScan(basePackages = {"commons"})
@ComponentScan(basePackages = { "commons", "nl.tudelft.sem.template.basket"})
@SpringBootApplication
//@EntityScan(basePackages = { "commons", "basket-microservice"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
