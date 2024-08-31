package nl.tudelft.sem.template.commons;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import commons.UserRole;
import org.apache.catalina.User;
import org.junit.jupiter.api.Test;

public class UserRoleTest {

    @Test
    void constructor() {
        UserRole us = new UserRole("role");
        assertNotNull(us);
    }

    @Test
    void getAuthority() {
        UserRole us = new UserRole("role");
        String res = "role";

        assertThat(us.getAuthority()).isEqualTo(res);
    }

    @Test
    void toStringTest() {
        UserRole us = new UserRole("role");
        assertThat(us.toString()).contains("role");
    }

}
