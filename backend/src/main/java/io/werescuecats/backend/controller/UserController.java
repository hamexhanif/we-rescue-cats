package io.werescuecats.backend.controller;

import io.werescuecats.backend.dto.LoginRequestDto;
import io.werescuecats.backend.dto.LoginResponseDto;
import io.werescuecats.backend.dto.UserDto;
import io.werescuecats.backend.dto.UserRegistrationDto;
import io.werescuecats.backend.entity.User;
import io.werescuecats.backend.entity.UserRole;
import io.werescuecats.backend.exception.UserAlreadyExistsException;
import io.werescuecats.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@Slf4j
public class UserController {
    
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    @RequestMapping("/login")
    public ResponseEntity<LoginResponseDto> loginUser(@RequestBody LoginRequestDto request) {
        log.info("Login attempt for user: {}", request.getEmail());
        
        try {
            Optional<User> userOpt = userService.authenticateUser(request.getEmail(), request.getPassword());
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                userService.updateLastLogin(user.getId());
                
                LoginResponseDto response = new LoginResponseDto();
                response.setSuccess(true);
                response.setMessage("Login successful");
                response.setUser(createSafeUserDto(user));
                
                return ResponseEntity.ok(response);
            } else {
                LoginResponseDto response = new LoginResponseDto();
                response.setSuccess(false);
                response.setMessage("Invalid credentials");
                
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            log.error("Error during login for user: {}", request.getEmail(), e);
            LoginResponseDto response = new LoginResponseDto();
            response.setSuccess(false);
            response.setMessage("Login failed");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        log.info("Fetching user with ID: {}", id);
        
        try {
            Optional<User> userOpt = userService.getUserById(id);
            
            if (userOpt.isPresent()) {
                UserDto userDto = createSafeUserDto(userOpt.get());
                return ResponseEntity.ok(userDto);
            }else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error fetching user with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody UserRegistrationDto request) {
        log.info("Registering new user: {}", request.getEmail());
        try {
            User user = createUserFromDto(request);
            User savedUser = userService.createUser(user);
            UserDto userDto = createSafeUserDto(savedUser);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
            
        } catch (UserAlreadyExistsException e) {
            log.warn("Registration failed - user already exists: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
            
        } catch (Exception e) {
            log.error("Error registering user: {}", request.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        log.info("Fetching all users");
        try {
            List<User> users = userService.getAllUsers();
            List<UserDto> userDtos = users.stream()
                    .map(this::createSafeUserDto)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(userDtos);
            
        } catch (Exception e) {
            log.error("Error fetching all users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Helper method to remove sensitive data
    private UserDto createSafeUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setStreetAddress(user.getStreetAddress());
        dto.setPostalCode(user.getPostalCode());
        dto.setRole(user.getRole());
        dto.setEnabled(user.isEnabled());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLogin(user.getLastLogin());

        return dto;
    }

    private User createUserFromDto(UserRegistrationDto dto) {
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPasswordHash(dto.getPassword()); 
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setStreetAddress(dto.getStreetAddress());
        user.setPostalCode(dto.getPostalCode());
        user.setRole(UserRole.USER);
        user.setTenantId("main");
        user.setEnabled(true);
        
        return user;
    }
}
