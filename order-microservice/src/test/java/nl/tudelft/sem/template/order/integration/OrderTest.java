package nl.tudelft.sem.template.order.integration;

import static org.assertj.core.api.Assertions.anyOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import commons.BasketInfo;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import commons.Coupon;
import commons.Ingredient;
import commons.Pizza;
import commons.authentication.AuthenticationManager;
import commons.UserRole;
import commons.authentication.JwtTokenVerify;
import nl.tudelft.sem.template.order.controllers.OrderController;
import nl.tudelft.sem.template.order.models.AddOrderRequestModel;
import nl.tudelft.sem.template.order.models.AddOrderResponseModel;
import nl.tudelft.sem.template.order.models.CancelOrderRequestModel;
import nl.tudelft.sem.template.order.models.RemoveCouponRequestModel;
import nl.tudelft.sem.template.order.models.CheckoutRequestModel;
import nl.tudelft.sem.template.order.services.RestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"test", "mockTokenVerifier", "mockAuthenticationManager", "customerOverview"})
@AutoConfigureMockMvc
public class OrderTest {

    private static final Logger LOGGER =
            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private transient JwtTokenVerify mockJwtTokenVerifier;

    private transient AuthenticationManager mockAuthenticationManager;

    private transient OrderController orderController;

    private AddOrderRequestModel request;
    private CheckoutRequestModel checkout;

    private ObjectMapper mapper = new ObjectMapper();

    private Coupon coupon;

    private transient RestService restService;

    public OrderTest() {
        mockAuthenticationManager = mock(AuthenticationManager.class);
        when(mockAuthenticationManager.getRole()).thenReturn("customer");
        when(mockAuthenticationManager.getNetId()).thenReturn("1");
        restService = mock(RestService.class);
        orderController = new OrderController(mockAuthenticationManager, restService);

        List<Pizza> pizzas = new ArrayList<>();
        pizzas.add(new Pizza("Pizza", new ArrayList<>()));
        request = new AddOrderRequestModel();
        coupon = new Coupon("code", 'A', 1.0, true);

        BasketInfo basketInfo = new BasketInfo(pizzas, 9.99f, coupon);
        request.setBasketInfo(basketInfo);

        LocalDateTime finishTime = LocalDateTime.of(2050, 11, 30, 10, 10);
        checkout = new CheckoutRequestModel();
        checkout.setFinishTime(finishTime);
        request.getBasketInfo().setStoreId(1);

        doReturn(ResponseEntity.of(Optional.of(request))).when(restService).getBasket(anyString());
        doReturn(ResponseEntity.ok("Success!")).when(restService)
                .removeCoupon(any(RemoveCouponRequestModel.class), anyString());
    }

    @BeforeEach
    public void setup() {
        when(mockAuthenticationManager.getRole()).thenReturn("customer");
        when(mockAuthenticationManager.getNetId()).thenReturn("1");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getUserIdFromToken(anyString())).thenReturn("1");
        when(mockJwtTokenVerifier.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));
    }

    private ResultActions postRequest(String path, Object data) throws Exception {
        return mockMvc.perform(post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(mapper.writeValueAsString(data))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "bearer-token"));
    }

    @Test
    public void submitCorrectOrderTest() throws Exception {
        ResponseEntity<AddOrderResponseModel> result = orderController.checkout(checkout, "token123");
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void cancelExistingOrderTest() throws Exception {
        ResponseEntity<AddOrderResponseModel> result = orderController.checkout(checkout, "token123");
        AddOrderResponseModel ret = result.getBody();
        int orderId = ret.getOrderId();

        CancelOrderRequestModel requestModel = new CancelOrderRequestModel();
        requestModel.setOrderId(orderId);
        ResponseEntity<String> response = orderController.cancelOrder(requestModel, "token123");
        assertThat(response.getBody()).isEqualTo("Success!");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void cancelNonExistingOrderTest() throws Exception {
        postRequest("/checkout", checkout);
        int orderId = -1;
        CancelOrderRequestModel requestModel = new CancelOrderRequestModel();
        requestModel.setOrderId(orderId);
        ResultActions response = postRequest("/cancel", requestModel);
        response.andExpect(status().isUnauthorized());
    }

    @Test
    public void checkoutNullPizzaOrderTest() {
        request.getBasketInfo().setPizzas(null);
        ResponseEntity<AddOrderResponseModel> result = orderController.checkout(checkout, "token123");
        AddOrderResponseModel ret = result.getBody();
        assertThat(ret.getOrderId()).isEqualTo(-1);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void cancelOrderThatIsNotMine() throws Exception {
        when(mockAuthenticationManager.getNetId()).thenReturn("0");
        ResponseEntity<AddOrderResponseModel> result = orderController.checkout(checkout, "token123");
        //ResultActions result = postRequest("/checkout", checkout);
        //AddOrderResponseModel ret = mapper.readValue(result.andReturn().getResponse().getContentAsString(),
        //        AddOrderResponseModel.class);
        AddOrderResponseModel ret = result.getBody();
        int orderId = ret.getOrderId();
        CancelOrderRequestModel requestModel = new CancelOrderRequestModel();
        requestModel.setOrderId(orderId);
        when(mockAuthenticationManager.getNetId()).thenReturn("1");
        ResponseEntity<String> response = orderController.cancelOrder(requestModel, "token123");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("user");
    }

    @Test
    public void cancelOrderAsManager() throws Exception {
        when(mockAuthenticationManager.getRole()).thenReturn("manager");
        ResponseEntity<AddOrderResponseModel> result = orderController.checkout(checkout, "token123");
        AddOrderResponseModel ret = result.getBody();

        int orderId = ret.getOrderId();
        CancelOrderRequestModel requestModel = new CancelOrderRequestModel();
        requestModel.setOrderId(orderId);
        ResponseEntity<String> response = orderController.cancelOrder(requestModel, "token123");
        assertThat(response.getBody()).isEqualTo("Success!");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // TO-DO:
        // test on notifications
    }

    @Test
    public void removeCouponTest() throws Exception {
        when(mockAuthenticationManager.getRole()).thenReturn("store");
        when(mockJwtTokenVerifier.getUserRoleFromToken(anyString())).thenReturn(new UserRole("store"));
        ResponseEntity<AddOrderResponseModel> result = orderController.checkout(checkout, "token123");
        AddOrderResponseModel ret = result.getBody();
        int orderId = ret.getOrderId();
        CancelOrderRequestModel requestModel = new CancelOrderRequestModel();
        requestModel.setOrderId(orderId);
        doReturn("store").when(mockAuthenticationManager).getRole();
        ResponseEntity<String> response = orderController.cancelOrder(requestModel, "token123");

        assertThat(response.getBody()).isEqualTo("Success!");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        verify(restService).removeCoupon(any(RemoveCouponRequestModel.class), anyString());
    }


    // For now I will only test whether the endpoint functions. I'll test the overview logic via unit tests, as each of
    // these integration tests takes a lot of time to set up.
    // will look later
    //@Test
    public void seeOrdersTest() throws Exception {
        // Arrange:
        // First set up mocks, then make sure some orders to be retrieved are there.
        when(mockAuthenticationManager.getNetId()).thenReturn("testUser");
        when(mockAuthenticationManager.getRole()).thenReturn("customer");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getUserIdFromToken(anyString())).thenReturn("testUser");
        when(mockJwtTokenVerifier.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));

        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(new Ingredient("Pepperoni", 3.50));
        List<Pizza> pizzas = new ArrayList<>();
        pizzas.add(new Pizza("spaghetti", ingredients));
        CheckoutRequestModel request = new CheckoutRequestModel();
        //request.setPizzas(pizzas);
        request.setFinishTime(LocalDateTime.of(2000, 1, 1, 1, 1));
        orderController.checkout(request, "token123");

        // Act
        ResultActions result = mockMvc.perform(get("/seeOrders")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isOk());

        String order = result.andReturn().getResponse().getContentAsString();
        assertThat(order).isEqualTo(
                "[{\"userId\":\"testUser\",\"orderId\":0,\"storeId\":\"delft\",\"finishTime\":\"2000-01-01T01:01:00\"}]"
        );
    }
}
