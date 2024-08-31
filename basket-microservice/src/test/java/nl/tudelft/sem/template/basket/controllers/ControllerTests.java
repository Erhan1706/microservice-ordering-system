package nl.tudelft.sem.template.basket.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.mockito.Mockito.when;

import commons.Ingredient;
import commons.UserRole;
import commons.authentication.AuthenticationManager;
import commons.authentication.JwtTokenVerify;
import nl.tudelft.sem.template.basket.services.IngredientService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"test", "mockAuthenticationManager", "mockTokenVerifier"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class ControllerTests {

    @MockBean
    private IngredientService ingredientService;

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
}
