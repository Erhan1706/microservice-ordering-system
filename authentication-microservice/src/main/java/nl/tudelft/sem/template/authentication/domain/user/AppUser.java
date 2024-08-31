package nl.tudelft.sem.template.authentication.domain.user;

import java.util.List;
import java.util.Objects;
import javax.persistence.*;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.authentication.domain.HasEvents;
import nl.tudelft.sem.template.authentication.domain.ingredients.IngredientConverter;
import nl.tudelft.sem.template.authentication.domain.user.events.PasswordWasChangedEvent;
import nl.tudelft.sem.template.authentication.domain.user.events.UserWasCreatedEvent;
import nl.tudelft.sem.template.authentication.domain.user.valueobjects.HashedPassword;
import nl.tudelft.sem.template.authentication.domain.user.valueobjects.NetId;
import nl.tudelft.sem.template.authentication.domain.user.valueobjects.UserRole;
import nl.tudelft.sem.template.authentication.domain.user.valueobjects.converters.HashedPasswordAttributeConverter;
import nl.tudelft.sem.template.authentication.domain.user.valueobjects.converters.NetIdAttributeConverter;
import nl.tudelft.sem.template.authentication.domain.user.valueobjects.converters.UserRoleAttributeConverter;

/**
 * A DDD entity representing an application user in our domain.
 */
@Entity
@Table(name = "users")
@NoArgsConstructor
public class AppUser extends HasEvents {
    /**
     * Identifier for the application user.
     */

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name = "net_id", nullable = false, unique = true)
    @Convert(converter = NetIdAttributeConverter.class)
    private NetId netId;

    @Column(name = "password_hash", nullable = false)
    @Convert(converter = HashedPasswordAttributeConverter.class)
    private HashedPassword password;

    @Column(name = "user_role", nullable = false)
    @Convert(converter = UserRoleAttributeConverter.class)
    private UserRole userRole;

    @Column(name = "allergies", nullable = false)
    @Convert(converter = IngredientConverter.class)
    private List<Long> allergies;

    /**
     * Create new application user.
     *
     * @param netId The NetId for the new user
     * @param password The password for the new user
     * @param userRole The role the user has
     */
    public AppUser(NetId netId, HashedPassword password, UserRole userRole, List<Long> allergy) {
        this.netId = netId;
        this.password = password;
        this.userRole = userRole;
        this.allergies = allergy;
        this.recordThat(new UserWasCreatedEvent(netId, userRole));
    }

    public void changePassword(HashedPassword password) {
        this.password = password;
        this.recordThat(new PasswordWasChangedEvent(this));
    }

    public NetId getNetId() {
        return netId;
    }

    public int getId() {
        return id;
    }

    public HashedPassword getPassword() {
        return password;
    }

    public UserRole getUserRole() {
        return userRole;
    }

    public List<Long> getAllergies() {
        return allergies;
    }

    public void addAllergies(List<Long> i) {
        this.allergies.addAll(i);
    }

    /**
     * Equality is only based on the identifier.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AppUser appUser = (AppUser) o;
        return id == (appUser.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(netId);
    }
}
