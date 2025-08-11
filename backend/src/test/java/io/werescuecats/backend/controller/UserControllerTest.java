package io.werescuecats.backend.controller;

import io.werescuecats.backend.dto.RegisterRequestDto;
import io.werescuecats.backend.dto.UserDto;
import io.werescuecats.backend.entity.User;
import io.werescuecats.backend.entity.UserRole;
import io.werescuecats.backend.exception.UserAlreadyExistsException;
import io.werescuecats.backend.exception.UserNotFoundException;
import io.werescuecats.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User user;
    private RegisterRequestDto registerRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());

        registerRequest = new RegisterRequestDto();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("Password123");
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
    }

    @Test
    void getUserById_ShouldReturnUser_WhenExists() {
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));

        ResponseEntity<UserDto> response = userController.getUserById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("test@example.com", response.getBody().getEmail());
        verify(userService).getUserById(1L);
    }

    @Test
    void getUserById_ShouldReturnNotFound_WhenDoesNotExist() {
        when(userService.getUserById(1L)).thenReturn(Optional.empty());

        ResponseEntity<UserDto> response = userController.getUserById(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService).getUserById(1L);
    }

    @Test
    void registerUser_ShouldReturnCreatedUser() {
        when(userService.createUser(any(User.class))).thenReturn(user);

        ResponseEntity<UserDto> response = userController.registerUser(registerRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("test@example.com", response.getBody().getEmail());
        verify(userService).createUser(any(User.class));
    }

    @Test
    void registerUser_ShouldReturnConflict_WhenUserExists() {
        when(userService.createUser(any(User.class)))
                .thenThrow(new UserAlreadyExistsException("User already exists"));

        ResponseEntity<UserDto> response = userController.registerUser(registerRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(userService).createUser(any(User.class));
    }

    @Test
    void getAllUsers_ShouldReturnUserList() {
        List<User> users = Arrays.asList(user);
        when(userService.getAllUsers()).thenReturn(users);

        ResponseEntity<List<UserDto>> response = userController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(userService).getAllUsers();
    }

    @Test
    void deleteUser_ShouldReturnNoContent_WhenUserExists() {
        doNothing().when(userService).deleteUser(1L);

        ResponseEntity<Void> response = userController.deleteUser(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteUser_ShouldReturnNotFound_WhenUserDoesNotExist() {
        doThrow(new UserNotFoundException("User not found")).when(userService).deleteUser(1L);

        ResponseEntity<Void> response = userController.deleteUser(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService).deleteUser(1L);
    }
}