package io.werescuecats.backend.controller;

import io.werescuecats.backend.dto.*;
import io.werescuecats.backend.entity.User;
import io.werescuecats.backend.security.CustomUserDetails;
import io.werescuecats.backend.security.JwtUtils;
import io.werescuecats.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@Slf4j
@AllArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> loginUser(@RequestBody @Valid LoginRequestDto request) {
        log.info("Login attempt for user: {}", request.getEmail());
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();
            
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("userId", user.getId());
            extraClaims.put("role", user.getRole().name());
            extraClaims.put("firstName", user.getFirstName());
            extraClaims.put("lastName", user.getLastName());
            
            String jwt = jwtUtils.generateToken(userDetails, extraClaims);
            
            userService.updateLastLogin(user.getId());
            
            LoginResponseDto response = new LoginResponseDto();
            response.setSuccess(true);
            response.setMessage("Login successful");
            response.setToken(jwt);
            response.setUser(createSafeUserDto(user));
            
            return ResponseEntity.ok(response);
            
        } catch (BadCredentialsException e) {
            log.warn("Invalid credentials for user: {}", request.getEmail());
            LoginResponseDto response = new LoginResponseDto();
            response.setSuccess(false);
            response.setMessage("Invalid credentials");
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            
        } catch (Exception e) {
            log.error("Error during login for user: {}", request.getEmail(), e);
            LoginResponseDto response = new LoginResponseDto();
            response.setSuccess(false);
            response.setMessage("Login failed");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> registerUser(@RequestBody @Valid RegisterRequestDto request) {
        log.info("Registration attempt for email: {}", request.getEmail());
        
        try {
            User newUser = new User();
            newUser.setEmail(request.getEmail());
            newUser.setPasswordHash(request.getPassword());
            newUser.setFirstName(request.getFirstName());
            newUser.setLastName(request.getLastName());
            newUser.setStreetAddress(request.getStreetAddress());
            newUser.setPostalCode(request.getPostalCode());
            
            User createdUser = userService.createUser(newUser);
            
            RegisterResponseDto response = new RegisterResponseDto();
            response.setSuccess(true);
            response.setMessage("Registration successful");
            response.setUser(createSafeUserDto(createdUser));
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("Error during registration for email: {}", request.getEmail(), e);
            RegisterResponseDto response = new RegisterResponseDto();
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

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
}
