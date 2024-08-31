package nl.tudelft.sem.template.order.authentication;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import commons.authentication.JwtAuthEntryPoint;
import commons.authentication.JwtReqFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;

public class JwtAuthenticationEntryPointTests {
    private transient JwtAuthEntryPoint jwtAuthenticationEntryPoint;

    private transient HttpServletRequest mockRequest;
    private transient HttpServletResponse mockResponse;
    private transient AuthenticationException dummyAuthenticationException;

    /**
     * Set up mocks.
     */
    @BeforeEach
    public void setup() {
        mockRequest = Mockito.mock(HttpServletRequest.class);
        mockResponse = Mockito.mock(HttpServletResponse.class);
        dummyAuthenticationException = Mockito.mock(AuthenticationException.class);

        jwtAuthenticationEntryPoint = new JwtAuthEntryPoint();
    }

    @Test
    public void commenceTest() throws ServletException, IOException {
        // Act
        jwtAuthenticationEntryPoint.commence(mockRequest, mockResponse, dummyAuthenticationException);

        // Assert
        verifyNoInteractions(mockRequest);
        verify(mockResponse).addHeader(JwtReqFilter.WWW_AUTHENTICATE_HEADER,
                JwtReqFilter.AUTHORIZATION_AUTH_SCHEME);
        verify(mockResponse).sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
        verifyNoMoreInteractions(mockResponse);
    }
}
