package nl.tudelft.sem.template.authentication.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.authentication.domain.user.valueobjects.NetId;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllergiesNetIdRequestModel {
    private NetId netId;
}
