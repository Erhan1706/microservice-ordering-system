package nl.tudelft.sem.template.order.models;

import lombok.Data;

@Data
public class AddOrderResponseModel {
    private int orderId;

    public AddOrderResponseModel() {}

    public AddOrderResponseModel(int orderId) {
        this.orderId = orderId;
    }

}
