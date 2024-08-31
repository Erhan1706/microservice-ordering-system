package nl.tudelft.sem.template.order.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.sun.istack.NotNull;
import commons.BasketInfo;
import commons.Coupon;
import commons.Pizza;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@SuppressWarnings("PMD")
@Data
@NoArgsConstructor
public class AddOrderRequestModel implements Serializable {
    private String time;
    private BasketInfo basketInfo;
}
