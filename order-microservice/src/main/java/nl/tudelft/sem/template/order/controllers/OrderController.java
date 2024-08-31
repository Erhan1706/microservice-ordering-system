package nl.tudelft.sem.template.order.controllers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import commons.BasketInfo;
import commons.Coupon;
import commons.Pizza;
import commons.authentication.AuthenticationManager;
import nl.tudelft.sem.template.order.Order;
import nl.tudelft.sem.template.order.models.AddOrderRequestModel;
import nl.tudelft.sem.template.order.models.AddOrderResponseModel;
import nl.tudelft.sem.template.order.models.CancelOrderRequestModel;
import nl.tudelft.sem.template.order.models.RemoveCouponRequestModel;
import nl.tudelft.sem.template.order.services.RestService;
import nl.tudelft.sem.template.order.models.CheckoutRequestModel;
import nl.tudelft.sem.template.order.order.overview.CustomerOverview;
import nl.tudelft.sem.template.order.order.overview.ManagerOverview;
import nl.tudelft.sem.template.order.order.overview.Overview;
import nl.tudelft.sem.template.order.order.overview.StoreOverview;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * Hello World example controller.
 * <p>
 * This controller shows how you can extract information from the JWT token.
 * </p>
 */
@SuppressWarnings("PMD")
@RestController
public class OrderController {

    // map to find orders by id
    private final transient TreeMap<Integer, Order> orderMap;

    private final transient RestService restService;
    private final transient AuthenticationManager authManager;

    /**
     * Instantiates a new controller.
     *
     * @param authManager Spring Security component used to authenticate and authorize the user
     */
    @Autowired
    public OrderController(AuthenticationManager authManager, RestService restService) {
        this.restService = restService;
        this.authManager = authManager;
        orderMap = new TreeMap<Integer, Order>();
    }

    /**
     * Adds an order to the map of orders.
     *
     * @return the orderId of the added order.
     */

    @PostMapping("/checkout")
    public ResponseEntity<AddOrderResponseModel> checkout(@RequestBody CheckoutRequestModel r,
                                                          @RequestHeader(name = "Authorization") String token) {
        token = token.substring(7);
        ResponseEntity<AddOrderRequestModel> req = restService.getBasket(token);
        BasketInfo request = req.getBody().getBasketInfo();

        if (request == null) {
            return ResponseEntity.badRequest().body(new AddOrderResponseModel(-1));
        }

        // initialize attributes of Order
        List<Pizza> pizzas = request.getPizzas();

        if (pizzas == null) {
            return ResponseEntity.badRequest().body(new AddOrderResponseModel(-1));
        }

        int orderId = orderMap.size() == 0 ? 0 : orderMap.lastKey() + 1;
        String storeId = String.valueOf(request.getStoreId());
        String userId = authManager.getNetId();

        Coupon coupon = request.getCoupon();
        float price = (float) request.getPrice();
        LocalDateTime finishTime = r.getFinishTime();

        // create order and add it to the queue and map
        Order order = new Order(orderId, storeId, userId, pizzas, coupon, price, finishTime);
        this.orderMap.put(orderId, order);

        // return the orderId so the client knows which id has been given to the order
        return ResponseEntity.ok(new AddOrderResponseModel(orderId));
    }

    /**
     * Removes an order from the map and the queue.
     *
     * @param request the order to remove.
     * @return success code.
     */
    @PostMapping("/cancel")
    public ResponseEntity<String> cancelOrder(@RequestBody CancelOrderRequestModel request,
                                              @RequestHeader(name = "Authorization") String token) {
        // get credentials
        String role = authManager.getRole();
        token = token.substring(7);

        // get information needed to validate request
        int orderId = request.getOrderId();
        Order order = orderMap.get(orderId);

        if (order == null)
            return ResponseEntity.badRequest().body("Order " + orderId + " was not found.");
        if (role == null)
            return ResponseEntity.badRequest().body("No role was found for this user.");
        String roleCheckResult = checkRoles(role, order, token);
        if (roleCheckResult != null)
            return ResponseEntity.badRequest().body(roleCheckResult);

        // if all checks are passed, the order can safely be removed
        this.orderMap.remove(orderId);
        return ResponseEntity.ok("Success!");
    }

    private String checkRoles(String role, Order order, String token) {
        String userId = authManager.getNetId();
        String storeId = order.getStoreId();
        LocalDateTime now = LocalDateTime.now();

        // for each role check if their actions are valid
        if (role == null) {
            return "No role was found for this user.";
        }
        if (role.equals("customer")) {
            // you can only delete orders that were made by you
            if (!userId.equals(order.getUserId())) {
                return "Order " + order.getOrderId()
                        + " does not belong to user " + userId + ".";
            }
            // only allow in-time cancellations
            if (Duration.between(now, order.getFinishTime()).toMinutes() < 30) {
                return "Order " + order.getOrderId() + " is too close to its finish time to be cancelled.";
            }
        } else if (role.equals("store")) {
            // stores can only cancel orders made to their store
            if (!storeId.equals(userId)) {
                return "Order " + order.getOrderId() + " does not belong to store " + userId + ".";
            }
            if (order.getCoupon() != null) {
                RemoveCouponRequestModel requestModel = new RemoveCouponRequestModel(order.getCoupon());
                ResponseEntity<String> resp = restService.removeCoupon(requestModel, token);
                if (!resp.getStatusCode().equals(HttpStatus.OK)) {
                    return resp.getBody();
                }
            }
        } else if (!role.equals("manager")) {
            // if the role is not one of the above three, the cancellation will not be accepted
            return "Role " + role + " does not exist.";
        }
        return null;
    }

    /**
     * Returns the orders belonging to the userRole of the user. For the customer that are the orders they made, for the
     * store that are the orders they have to prepare and for the manager that are all orders.
     *
     * @return the list orders.
     */
    @GetMapping("/seeOrders")
    public ResponseEntity<List<Order>> seeOrders() {
        String userRole = authManager.getRole();
        Overview overview;
        if (userRole.equals("customer")) {
            overview = new CustomerOverview(orderMap, authManager.getNetId());
        } else if (userRole.equals("store")) {
            // make store stored by userId of store instead of int: That's more like normal user, so we can use overlap
            overview = new StoreOverview(orderMap, authManager.getNetId());
        } else {
            overview = new ManagerOverview(orderMap);
        }
        return ResponseEntity.ok(overview.seeOrders());
    }
}
