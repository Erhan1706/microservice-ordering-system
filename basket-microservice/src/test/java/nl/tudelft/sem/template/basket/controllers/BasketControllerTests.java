package nl.tudelft.sem.template.basket.controllers;

import commons.*;
import commons.authentication.AuthenticationManager;
import commons.authentication.JwtTokenVerify;
import nl.tudelft.sem.template.basket.builder.PizzaBuilder;
import nl.tudelft.sem.template.basket.models.PizzaRequestModel;
import nl.tudelft.sem.template.basket.services.BasketService;
import nl.tudelft.sem.template.basket.services.CouponService;
import nl.tudelft.sem.template.basket.services.IngredientService;
import nl.tudelft.sem.template.basket.services.PizzaService;
import nl.tudelft.sem.template.basket.utils.JsonUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"test", "mockAuthenticationManager", "mockTokenVerifier"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class BasketControllerTests {

    @MockBean
    private PizzaService pizzaService;
    @MockBean
    private IngredientService ingredientService;
    @MockBean
    private CouponService couponService;
    @MockBean
    private BasketService basketService;
    @MockBean
    private PizzaBuilder builder;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private transient JwtTokenVerify mockJwtTokenVerify;

    @Autowired
    private transient AuthenticationManager mockAuthenticationManager;

    @Test
    public void getBasket() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("customer");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));
        when(basketService.getBasket("ExampleUser")).thenReturn(new Basket("ExampleUser"));

        ResultActions resultActions = mockMvc.perform(get("/api/basket/get")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        resultActions.andExpect(status().isOk());
    }

    @Test
    public void addPizzaToBasketInvalid() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("customer");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));
        when(basketService.getBasket("ExampleUser")).thenReturn(null);
        when(pizzaService.findByName(anyString())).thenReturn(new Pizza("Test", List.of()));
        when(builder.build()).thenReturn(null);

        ResultActions resultActions = mockMvc.perform(post("/api/basket/addPizza")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content("My Pizza"));
        resultActions.andExpect(status().isBadRequest());
        verify(basketService, times(1)).createBasket("ExampleUser");

        MvcResult result = mockMvc.perform(post("/api/basket/addPizza")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content("My Pizza")).andReturn();
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).isEqualTo("There is no such pizza as " + "My Pizza" + " on the menu.");
    }

    @Test
    public void addPizzaToBasketValid() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("customer");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));
        when(basketService.getBasket("ExampleUser")).thenReturn(new Basket("ExampleUser"));
        when(pizzaService.findByName(anyString())).thenReturn(new Pizza("Test", List.of()));
        when(builder.build()).thenReturn(new Pizza("Test", List.of()));

        ResultActions resultActions = mockMvc.perform(post("/api/basket/addPizza")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content("My Pizza"));
        resultActions.andExpect(status().isOk());
        verify(basketService, times(1))
                .addPizzaToBasket(anyString(), ArgumentMatchers.any());
    }

    @Test
    public void removePizzaFromBasketInvalid() throws Exception {
        Ingredient ingredient = new Ingredient("Cheese", 1.99);
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("customer");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));
        when(basketService.getBasket("ExampleUser")).thenReturn(
                new Basket("ExampleUser", new BasketInfo(List.of(new Pizza("Test", List.of(ingredient))), 1.00)));

        ResultActions resultActions = mockMvc.perform(delete("/api/basket/removePizza")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content("My Pizza"));
        resultActions.andExpect(status().isBadRequest());

        MvcResult result = mockMvc.perform(delete("/api/basket/removePizza")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content("My Pizza")).andReturn();
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).isEqualTo("There is no such pizza as " + "My Pizza" + " in your basket.");
    }

    @Test
    public void removePizzaFromBasketValid() throws Exception {
        Ingredient ingredient = new Ingredient("Cheese", 1.99);
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("customer");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));
        when(basketService.getBasket("ExampleUser")).thenReturn(
                new Basket("ExampleUser", new BasketInfo(List.of(new Pizza("My Pizza",
                    List.of(ingredient))), 1.00)));

        ResultActions resultActions = mockMvc.perform(delete("/api/basket/removePizza")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content("My Pizza"));
        resultActions.andExpect(status().isOk());

        MvcResult result = mockMvc.perform(delete("/api/basket/removePizza")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content("My Pizza")).andReturn();
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).isEqualTo("Pizza My Pizza is successfully removed from basket.");
    }

    @Test
    public void addCustomPizzaToBasketInvalidIngredient() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("customer");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));

        PizzaRequestModel model = new PizzaRequestModel("My Pizza", List.of("Pickles", "Cheese"));
        when(basketService.getBasket("ExampleUser")).thenReturn(null);
        when(ingredientService.getByName(anyString())).thenReturn(null);

        ResultActions resultActions = mockMvc.perform(post("/api/basket/addPizza/custom")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(model)));
        resultActions.andExpect(status().isBadRequest());
        verify(basketService, times(1)).createBasket("ExampleUser");

        MvcResult result = mockMvc.perform(post("/api/basket/addPizza/custom")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(model))).andReturn();
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).isEqualTo("We do not have " + "Pickles"
                + " as an ingredient on our inventory");
    }

    @Test
    public void addCustomPizzaToBasketValid() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("customer");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));

        Ingredient ingredient = new Ingredient("Cheese", 1.99);
        PizzaRequestModel model = new PizzaRequestModel("My Pizza", List.of("Pickles", "Cheese"));
        when(basketService.getBasket("ExampleUser")).thenReturn(
                new Basket("ExampleUser", new BasketInfo(List.of(new Pizza("My Pizza",
                    List.of(ingredient))), 1.00)));
        when(ingredientService.getByName(anyString())).thenReturn(new Ingredient());
        when(builder.build()).thenReturn(new Pizza("My Pizza", List.of(ingredient)));

        ResultActions resultActions = mockMvc.perform(post("/api/basket/addPizza/custom")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(model)));
        resultActions.andExpect(status().isOk());
    }

    @Test
    public void addCouponToEmptyBasket() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("customer");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));

        Ingredient ingredient = new Ingredient("Cheese", 1.99);
        when(basketService.getBasket("ExampleUser")).thenReturn(null);

        ResultActions resultActions = mockMvc.perform(post("/api/basket/applyCoupon")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content("abcd"));
        resultActions.andExpect(status().isBadRequest());

        MvcResult result = mockMvc.perform(post("/api/basket/applyCoupon")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content("abcd")).andReturn();
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).isEqualTo("Your basket is empty!");
    }

    @Test
    public void addInvalidCouponToBasket() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("customer");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));

        Ingredient ingredient = new Ingredient("Cheese", 1.99);
        when(basketService.getBasket("ExampleUser")).thenReturn(
                new Basket("ExampleUser", new BasketInfo(List.of(new Pizza("My Pizza",
                    List.of(ingredient))), 1.00)));
        when(couponService.getByCode(anyString())).thenReturn(null);

        ResultActions resultActions = mockMvc.perform(post("/api/basket/applyCoupon")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content("abcd"));
        resultActions.andExpect(status().isBadRequest());

        MvcResult result = mockMvc.perform(post("/api/basket/applyCoupon")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content("abcd")).andReturn();
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).isEqualTo("Coupon code: " + "abcd" + " is invalid.");
    }

    @Test
    public void addExistingCouponToBasket() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("customer");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));

        Ingredient ingredient = new Ingredient("Cheese", 1.99);
        when(basketService.getBasket("ExampleUser")).thenReturn(
                new Basket("ExampleUser", new BasketInfo(List.of(new Pizza("My Pizza",
                    List.of(ingredient))), 1.00, new Coupon("abcd"))));
        when(couponService.getByCode(anyString())).thenReturn(new Coupon("abcd"));

        ResultActions resultActions = mockMvc.perform(post("/api/basket/applyCoupon")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content("abcd"));
        resultActions.andExpect(status().isBadRequest());

        MvcResult result = mockMvc.perform(post("/api/basket/applyCoupon")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content("abcd")).andReturn();
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).isEqualTo("This coupon is already applied.");
    }

    @Test
    public void cheaperCouponExistsInBasket() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("customer");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));

        Ingredient ingredient = new Ingredient("Cheese", 1.99);
        when(basketService.getBasket("ExampleUser")).thenReturn(
                new Basket("ExampleUser", new BasketInfo(List.of(new Pizza("My Pizza",
                    List.of(ingredient))), 1.00)));
        when(couponService.getByCode(anyString())).thenReturn(new Coupon("abcd"));

        ResultActions resultActions = mockMvc.perform(post("/api/basket/applyCoupon")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content("abcd"));
        resultActions.andExpect(status().isBadRequest());

        MvcResult result = mockMvc.perform(post("/api/basket/applyCoupon")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content("abcd")).andReturn();
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).isEqualTo("Coupon has not been applied because there "
                + "is a cheaper coupon that has been applied already.");
    }

    @Test
    public void couponApplied() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("customer");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));

        Ingredient ingredient = new Ingredient("Cheese", 1.99);
        when(basketService.getBasket("ExampleUser")).thenReturn(
                new Basket("ExampleUser", new BasketInfo(List.of(new Pizza("My Pizza",
                    List.of(ingredient))), 90.00)));
        when(couponService.getByCode(anyString())).thenReturn(new Coupon("abcd"));
        when(basketService.applyCouponToBasket(anyString(), ArgumentMatchers.any())).thenReturn(true);

        ResultActions resultActions = mockMvc.perform(post("/api/basket/applyCoupon")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content("abcd"));
        resultActions.andExpect(status().isOk());

        MvcResult result = mockMvc.perform(post("/api/basket/applyCoupon")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content("abcd")).andReturn();
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).isEqualTo("30.0% discount coupon has been applied.\n"
                + "Current price: €90.00");
    }

    @Test
    public void couponAppliedGetOneFree() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("customer");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));

        Ingredient ingredient = new Ingredient("Cheese", 1.99);
        when(basketService.getBasket("ExampleUser")).thenReturn(
                new Basket("ExampleUser", new BasketInfo(List.of(new Pizza("My Pizza",
                    List.of(ingredient))), 90.00)));
        when(couponService.getByCode(anyString())).thenReturn(new Coupon("abcd", 'F'));
        when(basketService.applyCouponToBasket(anyString(), ArgumentMatchers.any())).thenReturn(true);

        ResultActions resultActions = mockMvc.perform(post("/api/basket/applyCoupon")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content("abcd"));
        resultActions.andExpect(status().isOk());

        MvcResult result = mockMvc.perform(post("/api/basket/applyCoupon")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content("abcd")).andReturn();
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).isEqualTo("Buy-one-get-one-free coupon has been applied.\n"
                + "Current price: €90.00");
    }

    @Test
    public void couponAppliedCustom() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("customer");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));

        Ingredient ingredient = new Ingredient("Cheese", 1.99);
        when(basketService.getBasket("ExampleUser")).thenReturn(
                new Basket("ExampleUser", new BasketInfo(List.of(new Pizza("My Pizza",
                    List.of(ingredient))), 90.00)));
        when(couponService.getByCode(anyString())).thenReturn(new Coupon("abcd", 'G'));
        when(basketService.applyCouponToBasket(anyString(), ArgumentMatchers.any())).thenReturn(true);

        ResultActions resultActions = mockMvc.perform(post("/api/basket/applyCoupon")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content("abcd"));
        resultActions.andExpect(status().isOk());

        MvcResult result = mockMvc.perform(post("/api/basket/applyCoupon")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content("abcd")).andReturn();
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).isEqualTo("Coupon has been applied.\n"
                + "Current price: €90.00");
    }

    @Test
    public void deleteCouponFromBasketInvalid() throws Exception {
        Ingredient ingredient = new Ingredient("Cheese", 1.99);
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("customer");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));
        when(basketService.getBasket("ExampleUser")).thenReturn(null);

        ResultActions resultActions = mockMvc.perform(delete("/api/basket/removeCoupon")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));
        resultActions.andExpect(status().isBadRequest());

        MvcResult result = mockMvc.perform(delete("/api/basket/removeCoupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer MockedToken"))
                .andReturn();
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).isEqualTo("You do not have any coupon applied in your basket!");
    }

    @Test
    public void deleteCouponFromBasketValid() throws Exception {
        Ingredient ingredient = new Ingredient("Cheese", 1.99);
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("customer");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));
        when(basketService.getBasket("ExampleUser")).thenReturn(
                new Basket("ExampleUser", new BasketInfo(List.of(new Pizza("My Pizza",
                    List.of(ingredient))), 90.00, new Coupon("abcd"))));

        ResultActions resultActions = mockMvc.perform(delete("/api/basket/removeCoupon")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));
        resultActions.andExpect(status().isOk());
        verify(basketService, times(1)).removeCouponFromBasket("ExampleUser");

        MvcResult result = mockMvc.perform(delete("/api/basket/removeCoupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer MockedToken"))
                .andReturn();
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).isEqualTo("Coupon code : abcd has been removed from your basket.\n"
                + "Current price: €90.00");
    }

    @Test
    public void getOverviewEmptyBasket() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("customer");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));
        when(basketService.getBasket("ExampleUser")).thenReturn(null);

        ResultActions resultActions = mockMvc.perform(get("/api/basket/overview")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));
        resultActions.andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/api/basket/overview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer MockedToken")).andReturn();
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).isEqualTo("Your basket is empty!");
    }

    @Test
    public void getOverviewNothingInBasket() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("customer");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));
        when(basketService.getBasket("ExampleUser")).thenReturn(new Basket("ExampleUser"));

        ResultActions resultActions = mockMvc.perform(get("/api/basket/overview")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));
        resultActions.andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/api/basket/overview")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")).andReturn();
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains("Pizzas:\n" + "Nothing is in the basket yet!\n" + "\n\nTotal: EUR 3.00"
                + "\nCoupon applied: "
                + "None");
    }

    @Test
    public void getOverviewNormalBasket() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("customer");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));
        Ingredient ingredient = new Ingredient("Cheese", 1.99);
        Basket testBasket = new Basket("ExampleUser", new BasketInfo(List.of(new Pizza("My Pizza",
            List.of(ingredient))), 90.00, new Coupon("abcd")));
        testBasket.setTime(LocalDateTime.of(2000, 10, 10, 2, 2));
        when(basketService.getBasket("ExampleUser")).thenReturn(
                testBasket);


        ResultActions resultActions = mockMvc.perform(get("/api/basket/overview")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));
        resultActions.andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/api/basket/overview")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")).andReturn();
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).isEqualTo("Pizzas:\n" + "My Pizza | EUR 4.99\n" + "\n\nTotal: EUR 90.00"
                + "\nCoupon applied: " + "abcd (30.0% discount coupon)" + "\n\nYour order will be ready at 10/10 2:2.");
    }
}
