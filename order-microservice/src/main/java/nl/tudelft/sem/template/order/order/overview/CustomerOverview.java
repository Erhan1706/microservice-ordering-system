package nl.tudelft.sem.template.order.order.overview;

import nl.tudelft.sem.template.order.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("PMD")
public class CustomerOverview implements Overview {

    private Map<Integer, Order> allOrders;
    private String userId;

    public CustomerOverview(Map<Integer, Order> allOrders, String userId) {
        this.allOrders = allOrders;
        this.userId = userId;
    }

    /**
     * Returns the orders for the client aka the orders they made.
     *
     * @return list of orders.
     */
    public List<Order> seeOrders() {
        List<Order> ownOrders = new ArrayList<>();
        for (Order order : allOrders.values()) {
            if (order.getUserId().equals(userId)) {
                ownOrders.add(order);
            }
        }
        return ownOrders;
    }

    // Currently, cancelOrder is never used, canceling already implemented in the endpoint
    /**
     * Cancels the order belonging to the orderId, but only if the order is made by this user.
     *
     * @param orderId the order to delete.
     * @return the order that was canceled
     * @throws IllegalOrderIdException in case the order is someone else's
     */
    public Order cancelOrder(int orderId) throws IllegalOrderIdException {
        Order toBeRemoved = allOrders.get(orderId);
        if (toBeRemoved.getUserId() == userId) {
            return allOrders.remove(orderId);
        } else {
            throw new IllegalOrderIdException();
        }
    }
}
