package nl.tudelft.sem.template.basket.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import commons.Coupon;
import commons.Ingredient;
import commons.Pizza;
import commons.UserRole;
import commons.authentication.AuthenticationManager;
import commons.authentication.JwtTokenVerify;
import nl.tudelft.sem.template.basket.models.CouponRequestModel;
import nl.tudelft.sem.template.basket.models.IngredientRequestModel;
import nl.tudelft.sem.template.basket.models.PizzaRequestModel;
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
import java.util.List;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"test", "mockAuthenticationManager", "mockTokenVerifier"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class RepoControllerTests {

    @MockBean
    private PizzaService pizzaService;
    @MockBean
    private IngredientService ingredientService;
    @MockBean
    private CouponService couponService;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private transient JwtTokenVerify mockJwtTokenVerify;

    @Autowired
    private transient AuthenticationManager mockAuthenticationManager;

    @Test
    public void getIngredientsTest() throws Exception {
        Ingredient ingredient = new Ingredient("Cheese", 1.99);
        Ingredient ingredient2 = new Ingredient("Salami", 2.87);
        Ingredient ingredient3 = new Ingredient("Tomato Sauce", 0.99);
        when(ingredientService.findAll()).thenReturn(List.of(ingredient, ingredient2, ingredient3));

        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("customer");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));

        ResultActions resultActions = mockMvc.perform(get("/api/repo/ingredients")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].name").value("Cheese"))
                .andExpect(jsonPath("$[0].price").value("1.99"));
    }

    @Test
    public void getPizzasTest() throws Exception {
        Ingredient ingredient = new Ingredient("Cheese", 1.99);
        Ingredient ingredient2 = new Ingredient("Salami", 2.87);
        Ingredient ingredient3 = new Ingredient("Tomato Sauce", 0.99);
        Pizza pizza1 = new Pizza("My pizza", List.of(ingredient, ingredient2));
        Pizza pizza2 = new Pizza("My pizza 2", List.of(ingredient, ingredient2, ingredient3));
        when(pizzaService.findAll()).thenReturn(List.of(pizza1, pizza2));

        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("customer");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));

        ResultActions resultActions = mockMvc.perform(get("/api/repo/pizzas/false")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("My pizza"))
                .andExpect(jsonPath("$[0].ingredients[0].name").value("Cheese"))
                .andExpect(jsonPath("$[1].ingredients[2].price").value("0.99"));
    }

    @Test
    public void getCouponsTest() throws Exception {
        Coupon discount = new Coupon("EXPL01");
        Coupon getOneFree = new Coupon("EXPL02", 'F');
        when(couponService.findAll()).thenReturn(List.of(discount, getOneFree));
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("customer");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));

        ResultActions resultActions = mockMvc.perform(get("/api/repo/coupons")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].code").value("EXPL01"))
                .andExpect(jsonPath("$[0].type").value("D"))
                .andExpect(jsonPath("$[0].rate").value("30.0"))
                .andExpect(jsonPath("$[0].limitedTime").value("false"));
    }

    @Test
    public void addCouponToRepositoryTestNoPermission() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("customer");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));

        CouponRequestModel model = new CouponRequestModel("EXPL09", 'F', 30, false);
        ResultActions resultActions = mockMvc.perform(post("/api/repo/coupons/addToRepo")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(model)));
        resultActions.andExpect(status().isBadRequest());

        MvcResult result = mockMvc.perform(post("/api/repo/coupons/addToRepo")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(model))).andReturn();
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).isEqualTo("Only stores and managers can add new coupons to the database!");
    }

    @Test
    public void addCouponToRepositoryInvalidCode() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("manager");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));
        // Invalid coupon code
        when(couponService.couponInvalid(ArgumentMatchers.any())).thenReturn(true);
        CouponRequestModel model = new CouponRequestModel("abcdef", 'F', 30, false);
        ResultActions resultActions = mockMvc.perform(post("/api/repo/coupons/addToRepo")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(model)));
        resultActions.andExpect(status().isBadRequest());

        MvcResult result = mockMvc.perform(post("/api/repo/coupons/addToRepo")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(model))).andReturn();
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody)
                .isEqualTo("The coupon code must be formatted with 4 characters followed by 2 numbers.");
    }

    @Test
    public void addCouponToRepositoryAlreadyExists() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("manager");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));

        when(couponService.exists(ArgumentMatchers.any())).thenReturn(true);
        CouponRequestModel model = new CouponRequestModel("abcdef", 'F', 30, false);
        ResultActions resultActions = mockMvc.perform(post("/api/repo/coupons/addToRepo")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(model)));
        resultActions.andExpect(status().isBadRequest());

        MvcResult result = mockMvc.perform(post("/api/repo/coupons/addToRepo")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(model))).andReturn();
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody)
                .isEqualTo("Coupon with the provided activation code already exists.");
    }

    @Test
    public void addCouponToRepositoryValid() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("manager");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));

        CouponRequestModel model = new CouponRequestModel("EXPL09", 'F', 30, false);
        ResultActions resultActions = mockMvc.perform(post("/api/repo/coupons/addToRepo")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(model)));
        resultActions.andExpect(status().isOk());

        MvcResult result = mockMvc.perform(post("/api/repo/coupons/addToRepo")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(model))).andReturn();
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody)
                .isEqualTo("Coupon code: " + "EXPL09" + " is added to the repository.");
    }

    @Test
    public void deleteCouponsNoPermission() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("customer");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));

        ResultActions resultActions = mockMvc.perform(delete("/api/repo/coupons/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content("test"));
        resultActions.andExpect(status().isBadRequest());

        MvcResult result = mockMvc.perform(delete("/api/repo/coupons/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content("test")).andReturn();
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody)
                .isEqualTo("Only stores and managers can delete coupons from the database!");
    }

    @Test
    public void deleteCouponsNull() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("manager");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));

        when(couponService.getByCode(anyString())).thenReturn(null);
        ResultActions resultActions = mockMvc.perform(delete("/api/repo/coupons/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content("test"));
        resultActions.andExpect(status().isBadRequest());

        MvcResult result = mockMvc.perform(delete("/api/repo/coupons/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content("test")).andReturn();
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody)
                .isEqualTo("Coupon code: " + "test" + " does not exist.");
    }

    @Test
    public void deleteCouponsValid() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("manager");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));

        when(couponService.getByCode(anyString())).thenReturn(new Coupon());
        ResultActions resultActions = mockMvc.perform(delete("/api/repo/coupons/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content("test"));
        resultActions.andExpect(status().isOk());

        MvcResult result = mockMvc.perform(delete("/api/repo/coupons/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content("test")).andReturn();
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody)
                .isEqualTo("Coupon code: " + "test" + " has been deleted.");
    }

    @Test
    public void addIngredientsNoPermission() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("customer");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));

        IngredientRequestModel model = new IngredientRequestModel("Pickles", 1.00);
        when(couponService.getByCode(anyString())).thenReturn(new Coupon());
        ResultActions resultActions = mockMvc.perform(post("/api/repo/ingredients/add")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(model)));
        resultActions.andExpect(status().isBadRequest());

        MvcResult result = mockMvc.perform(post("/api/repo/ingredients/add")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(model))).andReturn();
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody)
                .isEqualTo("Only stores and managers can add ingredients to the database!");
    }

    @Test
    public void addIngredientsInvalid() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("manager");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));
        // Empty name
        IngredientRequestModel model = new IngredientRequestModel("", 1.00);
        ResultActions resultActions = mockMvc.perform(post("/api/repo/ingredients/add")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(model)));
        resultActions.andExpect(status().isBadRequest());
        // Negative price
        IngredientRequestModel model2 = new IngredientRequestModel("Pickle", -1.00);
        ResultActions resultActions2 = mockMvc.perform(post("/api/repo/ingredients/add")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(model2)));
        resultActions2.andExpect(status().isBadRequest());
        when(ingredientService.exists(anyString())).thenReturn(true);
        // Already exists
        IngredientRequestModel model3 = new IngredientRequestModel("Pickle", 1.00);
        ResultActions resultActions3 = mockMvc.perform(post("/api/repo/ingredients/add")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(model2)));
        resultActions3.andExpect(status().isBadRequest());

        MvcResult result = mockMvc.perform(post("/api/repo/ingredients/add")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(model))).andReturn();
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody)
                .isEqualTo("This ingredient is invalid or already exists.");
    }

    @Test
    public void addIngredientsValid() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("manager");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));

        IngredientRequestModel model = new IngredientRequestModel("Pickles", 1.00);

        when(ingredientService.save(ArgumentMatchers.any())).thenReturn(new Ingredient("Pickles", 1.00));
        when(couponService.getByCode(anyString())).thenReturn(new Coupon());
        ResultActions resultActions = mockMvc.perform(post("/api/repo/ingredients/add")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(model)));
        resultActions.andExpect(status().isOk());
        verify(ingredientService, times(1)).save(ArgumentMatchers.any());

        MvcResult result = mockMvc.perform(post("/api/repo/ingredients/add")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(model))).andReturn();
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody)
                .isEqualTo("Pickles is added to the repository.");
    }

    @Test
    public void getAllergiesTest() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("customer");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));
        Ingredient ingredient = mock(Ingredient.class);
        when(ingredient.getId()).thenReturn(1L);
        when(ingredient.getName()).thenReturn("Cheese");
        when(ingredientService.findAll()).thenReturn(List.of(ingredient));

        ResultActions resultActions = mockMvc.perform(get("/api/repo/ingredients/allergies")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0]").value("1 - Cheese"));
    }

    @Test
    public void addPizzasNoPermission() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("customer");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));

        PizzaRequestModel model = new PizzaRequestModel("My Pizza", List.of("Pickles", "Cheese"));
        ResultActions resultActions = mockMvc.perform(post("/api/repo//pizzas/addToRepo")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(model)));
        resultActions.andExpect(status().isBadRequest());

        MvcResult result = mockMvc.perform(post("/api/repo//pizzas/addToRepo")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(model))).andReturn();
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody)
                .isEqualTo("Only stores and managers can add new pizzas to the database!");
    }

    @Test
    public void addPizzasInvalidIngredient() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("manager");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));
        when(ingredientService.getByName(anyString())).thenReturn(null);

        PizzaRequestModel model = new PizzaRequestModel("My Pizza", List.of("Pickles", "Cheese"));
        ResultActions resultActions = mockMvc.perform(post("/api/repo//pizzas/addToRepo")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(model)));
        resultActions.andExpect(status().isBadRequest());

        MvcResult result = mockMvc.perform(post("/api/repo//pizzas/addToRepo")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(model))).andReturn();
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody)
                .isEqualTo("Ingredient " + "Pickles" + " does not exist.");
    }

    @Test
    public void addPizzasInvalidPizza() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("manager");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));
        when(pizzaService.invalid(ArgumentMatchers.any())).thenReturn(true);
        when(ingredientService.getByName(anyString())).thenReturn(new Ingredient("Pickles", 1.00));

        PizzaRequestModel model = new PizzaRequestModel("My Pizza", List.of("Pickles", "Cheese"));
        ResultActions resultActions = mockMvc.perform(post("/api/repo//pizzas/addToRepo")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(model)));
        resultActions.andExpect(status().isBadRequest());

        MvcResult result = mockMvc.perform(post("/api/repo//pizzas/addToRepo")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(model))).andReturn();
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody)
                .isEqualTo("This pizza is invalid or already exists.");
    }

    @Test
    public void addPizzasValid() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("manager");
        when(mockJwtTokenVerify.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerify.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerify.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));
        when(pizzaService.invalid(ArgumentMatchers.any())).thenReturn(false);
        when(ingredientService.getByName(anyString())).thenReturn(new Ingredient("Pickles", 1.00));

        PizzaRequestModel model = new PizzaRequestModel("My Pizza", List.of("Pickles", "Cheese"));
        ResultActions resultActions = mockMvc.perform(post("/api/repo//pizzas/addToRepo")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(model)));
        resultActions.andExpect(status().isOk());
        verify(pizzaService, times(1)).save(ArgumentMatchers.any());

        MvcResult result = mockMvc.perform(post("/api/repo//pizzas/addToRepo")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(model))).andReturn();
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody)
                .isEqualTo("My Pizza is added to the repository.");
    }

}
