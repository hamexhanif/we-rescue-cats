package io.werescuecats.backend.service;

import io.werescuecats.backend.entity.User;
import io.werescuecats.backend.entity.UserRole;
import io.werescuecats.backend.exception.UserAlreadyExistsException;
import io.werescuecats.backend.exception.UserNotFoundException;
import io.werescuecats.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private User testAdmin;

    @BeforeEach
    void setUp() {
        testUser = createTestUser();
        testAdmin = createTestAdmin();
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

    @Test
    void getUserById_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals(testUser.getEmail(), result.get().getEmail());
        verify(userRepository).existsById(1L);
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_UserNotFound_ThrowsException() {
        when(userRepository.existsById(999L)).thenReturn(false);

        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> userService.getUserById(999L)
        );
        assertEquals("User not found with ID: 999", exception.getMessage());
        verify(userRepository).existsById(999L);
    }

    @Test
    void getUserByEmail_Success() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.getUserByEmail("test@example.com");

        assertTrue(result.isPresent());
        assertEquals(testUser.getEmail(), result.get().getEmail());
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void getUserByEmail_UserNotFound_ThrowsException() {
        when(userRepository.existsByEmail("notfound@example.com")).thenReturn(false);

        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> userService.getUserByEmail("notfound@example.com")
        );
        assertEquals("User not found with Email: notfound@example.com", exception.getMessage());
    }

    @Test
    void createUser_Success() {
        User newUser = new User();
        newUser.setEmail("new@example.com");
        newUser.setPasswordHash("plainPassword");
        newUser.setFirstName("New");
        newUser.setLastName("User");

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        User result = userService.createUser(newUser);

        assertNotNull(result);
        assertEquals(UserRole.USER, result.getRole());
        assertEquals("main", result.getTenantId());
        assertNotNull(result.getCreatedAt());
        verify(passwordEncoder).encode("plainPassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_UserAlreadyExists_ThrowsException() {
        User existingUser = new User();
        existingUser.setEmail("existing@example.com");
        
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(
            UserAlreadyExistsException.class,
            () -> userService.createUser(existingUser)
        );
        assertEquals("User with email already exists: existing@example.com", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_WithNullPassword_DoesNotEncodePassword() {
        User newUser = new User();
        newUser.setEmail("new@example.com");
        newUser.setPasswordHash(null);
        newUser.setFirstName("New");
        newUser.setLastName("User");

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        User result = userService.createUser(newUser);

        assertNotNull(result);
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_Success() {
        testUser.setFirstName("Updated John");
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.save(testUser)).thenReturn(testUser);

        User result = userService.updateUser(testUser);

        assertNotNull(result);
        assertEquals("Updated John", result.getFirstName());
        verify(userRepository).existsById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUser_UserNotFound_ThrowsException() {
        testUser.setId(999L);
        when(userRepository.existsById(999L)).thenReturn(false);

        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> userService.updateUser(testUser)
        );
        assertEquals("User not found with ID: 999", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateLastLogin_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.updateLastLogin(1L);

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateLastLogin_UserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        userService.updateLastLogin(999L);

        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getAllUsers_Success() {
        List<User> users = Arrays.asList(testUser, testAdmin);
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertTrue(result.contains(testUser));
        assertTrue(result.contains(testAdmin));
        verify(userRepository).findAll();
    }

    @Test
    void getAdminUsers_Success() {
        List<User> adminUsers = Arrays.asList(testAdmin);
        when(userRepository.findByRole(UserRole.ADMIN)).thenReturn(adminUsers);

        List<User> result = userService.getAdminUsers();

        assertEquals(1, result.size());
        assertEquals(UserRole.ADMIN, result.get(0).getRole());
        verify(userRepository).findByRole(UserRole.ADMIN);
    }

    @Test
    void authenticateUser_Success() {
        String email = "test@example.com";
        String password = "plainPassword";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPasswordHash())).thenReturn(true);

        Optional<User> result = userService.authenticateUser(email, password);

        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(password, testUser.getPasswordHash());
    }

    @Test
    void authenticateUser_InvalidPassword_ReturnsEmpty() {
        String email = "test@example.com";
        String password = "wrongPassword";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPasswordHash())).thenReturn(false);

        Optional<User> result = userService.authenticateUser(email, password);

        assertFalse(result.isPresent());
        verify(passwordEncoder).matches(password, testUser.getPasswordHash());
    }

    @Test
    void authenticateUser_DisabledUser_ReturnsEmpty() {
        String email = "test@example.com";
        String password = "plainPassword";
        testUser.setEnabled(false);
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.authenticateUser(email, password);

        assertFalse(result.isPresent());
    }

    @Test
    void authenticateUser_UserNotFound_ReturnsEmpty() {
        String email = "notfound@example.com";
        String password = "password";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        Optional<User> result = userService.authenticateUser(email, password);

        assertFalse(result.isPresent());
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_UserNotFound_ThrowsException() {
        when(userRepository.existsById(999L)).thenReturn(false);

        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> userService.deleteUser(999L)
        );
        assertEquals("User not found with ID: 999", exception.getMessage());
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void changePassword_Success() {
        String newPassword = "newPassword";
        String encodedPassword = "encodedNewPassword";
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
        when(userRepository.save(testUser)).thenReturn(testUser);

        User result = userService.changePassword(1L, newPassword);

        assertNotNull(result);
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(testUser);
    }

    @Test
    void changePassword_UserNotFound_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> userService.changePassword(999L, "newPassword")
        );
        assertEquals("User not found with ID: 999", exception.getMessage());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }
}