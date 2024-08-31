package nl.tudelft.sem.template.authentication.domain.user.valueobjects;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * A DDD value object representing a NetID in our domain.
 */
@EqualsAndHashCode
@NoArgsConstructor
public class NetId {
    private transient String netIdValue;

    public NetId(String netId) {
        // validate NetID
        this.netIdValue = netId;
    }

    @Override
    public String toString() {
        return netIdValue;
    }
}
