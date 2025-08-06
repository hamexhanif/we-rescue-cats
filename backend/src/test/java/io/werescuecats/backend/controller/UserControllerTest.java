package io.werescuecats.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.werescuecats.backend.config.SecurityConfig;
import io.werescuecats.backend.dto.LoginRequestDto;
import io.werescuecats.backend.dto.UserRegistrationDto;
import io.werescuecats.backend.entity.User;
import io.werescuecats.backend.entity.UserRole;
import io.werescuecats.backend.exception.UserAlreadyExistsException;
import io.werescuecats.backend.security.CustomUserDetailsService;
import io.werescuecats.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private User testAdmin;
    private LoginRequestDto loginRequest;
    private UserRegistrationDto registrationRequest;

    @BeforeEach
    void setUp() {
        testUser = createTestUser();
        testAdmin = createTestAdmin();
        loginRequest = createLoginRequest();
        registrationRequest = createRegistrationRequest();
    }

    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedPassword");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setStreetAddress("123 Main St");
        user.setPostalCode("12345");
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        user.setTenantId("main");
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    private User createTestAdmin() {
        User admin = new User();
        admin.setId(2L);
        admin.setEmail("admin@example.com");
        admin.setPasswordHash("hashedAdminPassword");
        admin.setFirstName("Jane");
        admin.setLastName("Smith");
        admin.setRole(UserRole.ADMIN);
        admin.setEnabled(true);
        admin.setTenantId("main");
        admin.setCreatedAt(LocalDateTime.now());
        return admin;
    }

    private LoginRequestDto createLoginRequest() {
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        return request;
    }

    private UserRegistrationDto createRegistrationRequest() {
        UserRegistrationDto request = new UserRegistrationDto();
        request.setEmail("newuser@example.com");
        request.setPassword("StrongPass123!");
        request.setFirstName("New");
        request.setLastName("User");
        request.setStreetAddress("456 Oak St");
        request.setPostalCode("67890");
        return request;
    }

    @Test
    void loginUser_Success() throws Exception {
        // Given
        when(userService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword()))
            .thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.user.email").value(testUser.getEmail()))
                .andExpect(jsonPath("$.user.firstName").value(testUser.getFirstName()))
                .andExpect(jsonPath("$.user.lastName").value(testUser.getLastName()));

        verify(userService).authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());
        verify(userService).updateLastLogin(testUser.getId());
    }

    @Test
    void loginUser_InvalidCredentials() throws Exception {
        // Given
        when(userService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword()))
            .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .with(csrf()))
                .andExpect(status().isUnauthorized());
                //.andExpect(jsonPath("$.success").value(false))
                //.andExpect(jsonPath("$.message").value("Invalid credentials"));

        verify(userService).authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());
        verify(userService, never()).updateLastLogin(any());
    }

    @Test
    void loginUser_ServiceException() throws Exception {
        // Given
        when(userService.authenticateUser(anyString(), anyString()))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Login failed"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_Success() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(get("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(testUser.getEmail()))
                .andExpect(jsonPath("$.firstName").value(testUser.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(testUser.getLastName()))
                .andExpect(jsonPath("$.role").value(testUser.getRole().toString()));

        verify(userService).getUserById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_NotFound() throws Exception {
        // Given
        when(userService.getUserById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/users/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userService).getUserById(999L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getUserById_AccessDenied() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(userService, never()).getUserById(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_ServiceException() throws Exception {
        // Given
        when(userService.getUserById(1L))
            .thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void registerUser_Success() throws Exception {
        // Given
        User savedUser = createTestUser();
        savedUser.setEmail(registrationRequest.getEmail());
        savedUser.setFirstName(registrationRequest.getFirstName());
        savedUser.setLastName(registrationRequest.getLastName());

        when(userService.createUser(any(User.class))).thenReturn(savedUser);

        // When & Then
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest))
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(registrationRequest.getEmail()))
                .andExpect(jsonPath("$.firstName").value(registrationRequest.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(registrationRequest.getLastName()))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService).createUser(any(User.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void registerUser_UserAlreadyExists() throws Exception {
        // Given
        when(userService.createUser(any(User.class)))
            .thenThrow(new UserAlreadyExistsException("User already exists"));

        // When & Then
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest))
                .with(csrf()))
                .andExpect(status().isConflict());

        verify(userService).createUser(any(User.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void registerUser_ValidationError() throws Exception {
        // Given
        UserRegistrationDto invalidRequest = new UserRegistrationDto();
        invalidRequest.setEmail("invalid-email"); // Invalid email format
        invalidRequest.setPassword("123"); // Too short password
        invalidRequest.setFirstName(""); // Empty first name

        // When & Then
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void registerUser_AccessDenied() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest))
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void registerUser_ServiceException() throws Exception {
        // Given
        when(userService.createUser(any(User.class)))
            .thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest))
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_Success() throws Exception {
        // Given
        List<User> users = Arrays.asList(testUser, testAdmin);
        when(userService.getAllUsers()).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].email").value(testUser.getEmail()))
                .andExpect(jsonPath("$[1].email").value(testAdmin.getEmail()));

        verify(userService).getAllUsers();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_EmptyList() throws Exception {
        // Given
        when(userService.getAllUsers()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(userService).getAllUsers();
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsers_AccessDenied() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(userService, never()).getAllUsers();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_ServiceException() throws Exception {
        // Given
        when(userService.getAllUsers())
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void loginUser_WithoutCsrf_ShouldWork() throws Exception {
        // Given - CSRF should be disabled for REST API
        when(userService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword()))
            .thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void loginUser_InvalidJson() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).authenticateUser(any(), any());
    }

    @Test
    void loginUser_MissingPassword() throws Exception {
        LoginRequestDto incompleteRequest = new LoginRequestDto();
        incompleteRequest.setEmail("test@example.com");

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(incompleteRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).authenticateUser(any(), any());
    }
}
