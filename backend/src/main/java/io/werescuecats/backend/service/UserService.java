package io.werescuecats.backend.service;

import io.werescuecats.backend.entity.User;
import io.werescuecats.backend.entity.UserRole;
import io.werescuecats.backend.exception.UserAlreadyExistsException;
import io.werescuecats.backend.exception.UserNotFoundException;
import io.werescuecats.backend.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public Optional<User> getUserById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with ID: " + id);
        }
        return userRepository.findById(id);
    }
    
    public Optional<User> getUserByEmail(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new UserNotFoundException("User not found with Email: " + email);
        }
        return userRepository.findByEmail(email);
    }
    
    @Transactional
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistsException("User with email already exists: " + user.getEmail());
        }
        
        if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }

        if (user.getRole() == null) {
            user.setRole(UserRole.USER);
        }
        if (user.getTenantId() == null || user.getTenantId().isEmpty()) {
            user.setTenantId("main");
        }
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }
        
        log.info("Creating new user: {}", user.getEmail());
        return userRepository.save(user);
    }
    
    @Transactional
    public User updateUser(User user) {
        if (!userRepository.existsById(user.getId())) {
            throw new UserNotFoundException("User not found with ID: " + user.getId());
        }

        log.info("Updating user: {}", user.getEmail());
        return userRepository.save(user);
    }
    
    @Transactional
    public void updateLastLogin(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            log.debug("Updated last login for user ID: {}", userId);
        } else {
            log.warn("Attempted to update last login for non-existent user ID: {}", userId);
        }
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public List<User> getAdminUsers() {
        return userRepository.findByRole(UserRole.ADMIN);
    }
    
    public Optional<User> authenticateUser(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.isEnabled() && passwordEncoder.matches(password, user.getPasswordHash())) {
                return Optional.of(user);
            }
        }
        
        log.debug("Authentication failed for email: {}", email);
        return Optional.empty();
    }

    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
        
        userRepository.deleteById(userId);
        log.info("Deleted user with ID: {}", userId);
    }

    @Transactional
    public User changePassword(Long userId, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            User savedUser = userRepository.save(user);
            log.info("Password changed for user ID: {}", userId);
            return savedUser;
        } else {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
    }
}
