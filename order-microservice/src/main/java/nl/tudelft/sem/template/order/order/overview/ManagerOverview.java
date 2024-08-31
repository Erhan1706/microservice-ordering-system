package nl.tudelft.sem.template.order.order.overview;

import nl.tudelft.sem.template.order.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("PMD")
public class ManagerOverview implements Overview {

    private Map<Integer, Order> allOrders;

    public ManagerOverview(Map<Integer, Order> allOrders) {
        this.allOrders = allOrders;
    }

    /**
     * Returns all orders.
     *
     * @return a list of all orders.
     */
    public List<Order> seeOrders() {
        // managers can see all orders
        return new ArrayList<>(allOrders.values());
    }

    // Currently cancelOrder is never used, canceling already implemented in the endpoint

    /**
     * Cancels the order belonging to the orderId.
     *
     * @param orderId the order to delete.
     * @return the order that was canceled.
     */
    public Order cancelOrder(int orderId) {
        return allOrders.remove(orderId);
    }

}
