package nl.tudelft.sem.template.order.order.overview;

import nl.tudelft.sem.template.order.Order;
import java.util.List;

public interface Overview {

    public List<Order> seeOrders();

    public Order cancelOrder(int orderId) throws IllegalOrderIdException;
}
