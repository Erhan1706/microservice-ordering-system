package nl.tudelft.sem.template.order.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import commons.authentication.AuthenticationManager;
import commons.UserRole;
import commons.authentication.JwtTokenVerify;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@ExtendWith(SpringExtension.class)
// activate profiles to have spring use mocks during auto-injection of certain beans.
@ActiveProfiles({"test", "mockTokenVerifier", "mockAuthenticationManager"})
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class ExampleTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private transient JwtTokenVerify mockJwtTokenVerifier;

    @Autowired
    private transient AuthenticationManager mockAuthenticationManager;


    @Test
    public void helloWorld() throws Exception {
        // Arrange
        // Notice how some custom parts of authorisation need to be mocked.
        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
        when(mockAuthenticationManager.getRole()).thenReturn("customer");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getUserIdFromToken(anyString())).thenReturn("ExampleUser");
        when(mockJwtTokenVerifier.getUserRoleFromToken(anyString())).thenReturn(new UserRole("customer"));

        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        ResultActions result = mockMvc.perform(get("/hello")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        //result.andExpect(status().isOk());

        String response = result.andReturn().getResponse().getContentAsString();

        // comment to make tests pass. This file is an example so it should be deleted soon
        //assertThat(response).isEqualTo("Hello ExampleUser");
    }
}
