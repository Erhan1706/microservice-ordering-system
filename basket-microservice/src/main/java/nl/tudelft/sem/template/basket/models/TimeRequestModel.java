package nl.tudelft.sem.template.basket.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class TimeRequestModel {

    /**
     * True if the order is to be picked up tomorrow, false if today.
     * This is to make it easier if the customer is ordering around midnight
     * and the pickup time has to go over to the next day.
     */
    private boolean tomorrow;
    private int hour;
    private int minute;
}
