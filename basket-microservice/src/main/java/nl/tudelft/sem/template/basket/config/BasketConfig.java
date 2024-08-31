package nl.tudelft.sem.template.basket.config;

import commons.Ingredient;
import commons.Pizza;
import nl.tudelft.sem.template.basket.repositories.IngredientRepository;
import nl.tudelft.sem.template.basket.repositories.PizzaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class BasketConfig {

    /*
    This is method to populate the database with ingredients and pizzas when starting the app.
     */
    @Bean
    CommandLineRunner commandLineRunner(IngredientRepository ingredientRepository, PizzaRepository pizzaRepository) {
        return args -> {
            ingredientRepository.flush();
            pizzaRepository.flush();

            Ingredient cheese = new Ingredient("Mozzarella Cheese", 2.50);
            Ingredient parmesan = new Ingredient("Parmesan Cheese", 2.30);
            Ingredient pepperoni = new Ingredient("Pepperoni", 3.50);
            Ingredient tomatoSauce = new Ingredient("Tomato Sauce", 2.80);
            Ingredient oregano = new Ingredient("Oregano", 1.99);
            Ingredient chicken = new Ingredient("Chicken", 4.50);
            Ingredient bbqSauce = new Ingredient("BBQ Sauce", 2.99);
            Ingredient pineapple = new Ingredient("Pineapple", 0.99);
            Ingredient shrimp = new Ingredient("Shrimp", 3.99);
            Ingredient ham = new Ingredient("Ham", 3.00);
            Ingredient onion = new Ingredient("Onion", 3.10);
            Ingredient mushroom = new Ingredient("Mushroom", 0.87);
            ingredientRepository.saveAll(List.of(cheese, pepperoni, tomatoSauce, oregano, chicken, bbqSauce, pineapple,
                    shrimp, parmesan, ham, onion, mushroom));

            Pizza margherita = new Pizza("Margherita", List.of(cheese, tomatoSauce, oregano));
            Pizza hawaii = new Pizza("Hawaii", List.of(cheese, tomatoSauce, oregano, pineapple, shrimp));
            Pizza bbqChicken = new Pizza("BBQ Chicken", List.of(bbqSauce, chicken, parmesan, onion));
            Pizza vegan = new Pizza("Vegan", List.of(tomatoSauce, oregano, mushroom, onion));
            Pizza pepperoniPizza = new Pizza("Pepperoni", List.of(tomatoSauce, pepperoni, cheese));
            Pizza cheesy = new Pizza("Cheesy", List.of(tomatoSauce, parmesan, cheese, oregano, ham));
            Pizza prosciutto = new Pizza("Prosciutto", List.of(tomatoSauce, cheese, onion, ham));
            pizzaRepository.saveAll(List.of(margherita, hawaii, bbqChicken, vegan, pepperoniPizza,
                    cheesy, prosciutto));
        };
    }
}
