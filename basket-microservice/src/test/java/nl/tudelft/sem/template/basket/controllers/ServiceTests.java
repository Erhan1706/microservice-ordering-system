package nl.tudelft.sem.template.basket.controllers;


import static org.assertj.core.api.Assertions.assertThat;

import commons.Coupon;
import commons.Ingredient;
import commons.Pizza;
import nl.tudelft.sem.template.basket.repositories.CouponRepository;
import nl.tudelft.sem.template.basket.repositories.IngredientRepository;
import nl.tudelft.sem.template.basket.repositories.PizzaRepository;
import nl.tudelft.sem.template.basket.services.CouponService;
import nl.tudelft.sem.template.basket.services.IngredientService;
import nl.tudelft.sem.template.basket.services.PizzaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;

import java.util.List;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class ServiceTests {

    @MockBean
    private transient PizzaRepository pizzaRepository;

    @MockBean
    private transient CouponRepository couponRepository;

    @MockBean
    private transient IngredientRepository ingredientRepository;

    @Autowired
    private MockMvc mockMvc;

    private IngredientService ingredientService;
    private CouponService couponService;
    private PizzaService pizzaService;

    @BeforeEach
    void setup() {
        ingredientService = new IngredientService(ingredientRepository);
        couponService = new CouponService(couponRepository);
        pizzaService = new PizzaService(pizzaRepository);
    }

    @Test
    void getIngredientByNameRepositoryTest() {
        Ingredient ingredient = new Ingredient("Cheese", 1.99);
        ingredientRepository.save(ingredient);
        assertThat(ingredientRepository.getIngredientByName("Cheese")).isNotNull();
    }

    @Test
    void findAllIngredientsTest() {
        Ingredient ingredient = new Ingredient("Cheese", 1.99);
        Ingredient ingredient2 = new Ingredient("Salami", 2.87);
        Ingredient ingredient3 = new Ingredient("Tomato Sauce", 0.99);
        ingredientRepository.saveAll(List.of(ingredient, ingredient2, ingredient3));
        when(ingredientRepository.findAll()).thenReturn(List.of(ingredient, ingredient2, ingredient3));
        assertThat(ingredientService.findAll()).containsAll(List.of(ingredient, ingredient2, ingredient3));
    }

    @Test
    void pizzaExistsTest() {
        Ingredient ingredient = new Ingredient("Cheese", 1.99);
        Ingredient ingredient2 = new Ingredient("Salami", 2.87);
        Ingredient ingredient3 = new Ingredient("Tomato Sauce", 0.99);
        ingredientRepository.saveAll(List.of(ingredient, ingredient2, ingredient3));
        Pizza pizza = new Pizza("Margherita Pizza", List.of(ingredient, ingredient2, ingredient3));
        pizzaRepository.save(pizza);
        when(pizzaRepository.findAll()).thenReturn(List.of(pizza));
        assertThat(pizzaService.exists(pizza)).isTrue();
    }

    @Test
    void pizzaDoesNotExistTest() {
        Ingredient ingredient = new Ingredient("Cheese", 1.99);
        Ingredient ingredient2 = new Ingredient("Salami", 2.87);
        Ingredient ingredient3 = new Ingredient("Tomato Sauce", 0.99);
        ingredientRepository.saveAll(List.of(ingredient, ingredient2, ingredient3));
        Pizza pizza = new Pizza("Margherita Pizza", List.of(ingredient, ingredient2, ingredient3));
        pizzaRepository.save(pizza);
        Pizza pizza2 = new Pizza("Normal Pizza", List.of(ingredient, ingredient2));
        assertThat(pizzaService.exists(pizza2)).isFalse();
    }

    @Test
    void couponRepositoryTest() {
        Coupon discount = new Coupon("EXPL01");
        couponRepository.save(discount);
        when(couponRepository.findAll()).thenReturn(List.of(discount));
        assertThat(couponRepository.findAll()).contains(discount);
    }

    @Test
    void addCouponTest() {
        Coupon a = new Coupon("TEST01");
        couponService.save(a);
        when(couponRepository.findAll()).thenReturn(List.of(a));
        assertThat(couponService.exists("TEST01")).isTrue();
    }

    @Test
    void deleteCouponTest() {
        Coupon a = new Coupon("TEST01");
        Coupon b = new Coupon("TEST02");
        couponService.save(a);
        couponService.save(b);
        couponService.delete(a);
        assertThat(couponRepository.findAll()).doesNotContain(a);
    }

}

