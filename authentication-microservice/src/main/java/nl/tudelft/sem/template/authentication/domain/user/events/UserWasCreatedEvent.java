package nl.tudelft.sem.template.authentication.domain.user.events;

import nl.tudelft.sem.template.authentication.domain.user.valueobjects.NetId;
import nl.tudelft.sem.template.authentication.domain.user.valueobjects.UserRole;

/**
 * A DDD domain event that indicated a user was created.
 */
public class UserWasCreatedEvent {
    private final NetId netId;
    private final UserRole userRole;

    public UserWasCreatedEvent(NetId netId, UserRole userRole) {
        this.netId = netId;
        this.userRole = userRole;
    }

    public NetId getNetId() {
        return this.netId;
    }

    public UserRole getUserRole() {
        return this.userRole;
    }
}
