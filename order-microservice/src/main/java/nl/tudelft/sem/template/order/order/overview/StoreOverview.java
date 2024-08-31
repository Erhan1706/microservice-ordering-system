package nl.tudelft.sem.template.order.order.overview;

import nl.tudelft.sem.template.order.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("PMD")
public class StoreOverview implements Overview {

    private Map<Integer, Order> allOrders;
    private String storeId;

    public StoreOverview(Map<Integer, Order> allOrders, String storeId) {
        this.allOrders = allOrders;
        this.storeId = storeId;
    }

    /**
     * Returns the orders for the store aka the orders it has to prepare.
     *
     * @return list of orders.
     */
    public List<Order> seeOrders() {
        List<Order> storeOrders = new ArrayList<>();
        for (Order order : allOrders.values()) {
            if (order.getStoreId().equals(storeId)) {
                storeOrders.add(order);
            }
        }
        return storeOrders;
    }

    // Currently cancelOrder is never used, canceling already implemented in the endpoint
    // Maybe integrate the time somehow such that only cleared orders can be canceled:
    // Did stores have the liberty to just cancel or was that only manager?

    /**
     * Cancels the order belonging to the orderId, but only if the order is made at this store.
     *
     * @param orderId the order to delete.
     * @return the order that was canceled
     * @throws IllegalOrderIdException in case the order is for a different store
     */
    public Order cancelOrder(int orderId) throws IllegalOrderIdException {
        Order toBeRemoved = allOrders.get(orderId);
        if (toBeRemoved.getStoreId().equals(storeId)) {
            return allOrders.remove(orderId);
        } else {
            throw new IllegalOrderIdException();
        }
    }
}
