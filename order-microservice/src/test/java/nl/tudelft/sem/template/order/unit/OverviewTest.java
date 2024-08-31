package nl.tudelft.sem.template.order.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import commons.Ingredient;
import commons.Pizza;
import nl.tudelft.sem.template.order.Order;
import nl.tudelft.sem.template.order.order.overview.CustomerOverview;
import nl.tudelft.sem.template.order.order.overview.IllegalOrderIdException;
import nl.tudelft.sem.template.order.order.overview.ManagerOverview;
import nl.tudelft.sem.template.order.order.overview.StoreOverview;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class OverviewTest {

    private HashMap<Integer, Order> orders;
    private List<Pizza> pizzas;
    private List<Ingredient> ingredients;
    private Order order1;
    private Order order2;

    @BeforeEach
    public void setup() {
        ingredients = new ArrayList<>();
        ingredients.add(new Ingredient("Pepperoni", 3.50));
        pizzas = new ArrayList<>();
        pizzas.add(new Pizza("margerita", ingredients));
        LocalDateTime ldt = LocalDateTime.of(2000, 1, 1, 1, 1);
        order1 = new Order(1, "delft", "testUser1", pizzas, null, 900.00F, ldt);
        order2 = new Order(2, "brasil", "testUser2", pizzas, null, 900.00F, ldt);
        orders = new HashMap<>();
        orders.put(1, order1);
        orders.put(2, order2);
    }

    // ManagerOverview tests
    @Test
    public void managerOverviewSeeOrdersTest() {
        // Arrange
        List<Order> expected = new ArrayList<>(List.of(order1, order2));
        ManagerOverview manOverview = new ManagerOverview(orders);

        // Act
        List<Order> received = manOverview.seeOrders();

        // Assert
        assertThat(received).containsAll(expected);
        assertThat(received.size()).isEqualTo(2);
    }

    @Test
    public void managerOverviewCancelOrderTest() {
        // Arrange
        ManagerOverview managerOverview = new ManagerOverview(orders);
        Order expected = order2;

        // Act
        Order received = managerOverview.cancelOrder(2);

        // Assert
        assertThat(received).isEqualTo(expected);
    }

    // StoreOverview tests
    @Test
    public void storeOverviewSeeOrdersTest() {
        // Arrange
        StoreOverview storeOverview = new StoreOverview(orders, "delft");
        List<Order> expected = new ArrayList<>(List.of(order1));

        // Act
        List<Order> received = storeOverview.seeOrders();

        // Arrange
        assertThat(received).containsAll(expected);
        assertThat(received.size()).isEqualTo(1);
    }

    @Test
    public void storeOverviewCancelOrderValidTest() throws IllegalOrderIdException {
        // Arrange
        StoreOverview storeOverview = new StoreOverview(orders, "delft");
        Order expected = order1;

        // Act
        Order received = storeOverview.cancelOrder(1);

        // Assert
        assertThat(received).isEqualTo(expected);
    }

    @Test
    public void storeOverviewCancelOrderInvalidTest() {
        // Arrange
        StoreOverview storeOverview = new StoreOverview(orders, "delft");

        // Act + Assert
        assertThatThrownBy(() -> storeOverview.cancelOrder(2))
                .isInstanceOf(IllegalOrderIdException.class);
        assertThatThrownBy(() -> storeOverview.cancelOrder(2))
                .hasMessage("The order you are trying to cancel is not yours!");
    }

    // CustomerOverview tests
    @Test
    public void customerOverviewSeeOrdersTest() {
        // Arrange
        String userId = "testUser1";
        List<Order> expected = new ArrayList<>(List.of(order1));
        CustomerOverview customerOverview = new CustomerOverview(orders, userId);

        // Act
        List<Order> received = customerOverview.seeOrders();

        // Assert
        assertThat(received).containsAll(expected);
        assertThat(received.size()).isEqualTo(1);
    }

    @Test
    public void customerOverviewCancelOrderValidTest() throws IllegalOrderIdException {
        // Arrange
        String userId = "testUser1";
        Order expected = order1;
        CustomerOverview customerOverview = new CustomerOverview(orders, userId);

        // Act
        Order received = customerOverview.cancelOrder(1);

        // Assert
        assertThat(received).isEqualTo(expected);
    }

    @Test
    public void customerOverviewCancelOrderInvalidTest() {
        // Arrange
        String userId = "testUser3";
        CustomerOverview customerOverview = new CustomerOverview(orders, userId);

        // Act + Assert
        assertThatThrownBy(() -> customerOverview.cancelOrder(1))
                .isInstanceOf(IllegalOrderIdException.class);
        assertThatThrownBy(() -> customerOverview.cancelOrder(1))
                .hasMessage("The order you are trying to cancel is not yours!");
    }
}
