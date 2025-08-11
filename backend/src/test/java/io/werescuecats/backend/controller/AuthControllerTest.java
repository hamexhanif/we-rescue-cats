package io.werescuecats.backend.controller;

import io.werescuecats.backend.dto.*;
import io.werescuecats.backend.entity.User;
import io.werescuecats.backend.entity.UserRole;
import io.werescuecats.backend.security.CustomUserDetails;
import io.werescuecats.backend.security.JwtUtils;
import io.werescuecats.backend.service.AdoptionService;
import io.werescuecats.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AdoptionService adoptionService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthController authController;

    private User user;
    private LoginRequestDto loginRequest;
    private RegisterRequestDto registerRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(UserRole.USER);
        user.setCreatedAt(LocalDateTime.now());

        loginRequest = new LoginRequestDto();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        registerRequest = new RegisterRequestDto();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("Password123");
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");

    }

    @Test
    void loginUser_ShouldReturnSuccess_WhenCredentialsValid() {
        
        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateToken(eq(userDetails), anyMap())).thenReturn("mock-jwt-token");
        doNothing().when(userService).updateLastLogin(1L);
        
        ResponseEntity<LoginResponseDto> response = authController.loginUser(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Login successful", response.getBody().getMessage());
        assertEquals("mock-jwt-token", response.getBody().getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).updateLastLogin(1L);
    }

    @Test
    void loginUser_ShouldReturnUnauthorized_WhenCredentialsInvalid() {
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        ResponseEntity<LoginResponseDto> response = authController.loginUser(loginRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(adoptionService, never()).getAnonymousAdoptionData();
    }

    @Test
    void loginUser_ShouldReturnInternalServerError_WhenUnexpectedExceptionOccurs() {
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<LoginResponseDto> response = authController.loginUser(loginRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Login failed", response.getBody().getMessage());
    }

    @Test
    void registerUser_ShouldReturnCreated_WhenRegistrationSuccessful() {
        
        User createdUser = new User();
        createdUser.setId(2L);
        createdUser.setEmail(registerRequest.getEmail());
        createdUser.setFirstName(registerRequest.getFirstName());
        createdUser.setLastName(registerRequest.getLastName());
        createdUser.setRole(UserRole.USER);

        when(userService.createUser(any(User.class))).thenReturn(createdUser);

        ResponseEntity<RegisterResponseDto> response = authController.registerUser(registerRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Registration successful", response.getBody().getMessage());
        assertNotNull(response.getBody().getUser());
        assertEquals(createdUser.getEmail(), response.getBody().getUser().getEmail());
        verify(userService).createUser(any(User.class));
    }

    @Test
    void registerUser_ShouldReturnBadRequest_WhenRegistrationFails() {
        
        when(userService.createUser(any(User.class)))
                .thenThrow(new IllegalArgumentException("Email already exists"));

        ResponseEntity<RegisterResponseDto> response = authController.registerUser(registerRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Email already exists", response.getBody().getMessage());
    }
}
