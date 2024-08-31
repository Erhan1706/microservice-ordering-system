package nl.tudelft.sem.template.order.models;

import lombok.Data;

import java.io.Serializable;

@SuppressWarnings("PMD")
@Data
public class CancelOrderRequestModel implements Serializable {
    private int orderId;

    public CancelOrderRequestModel() {}
}
