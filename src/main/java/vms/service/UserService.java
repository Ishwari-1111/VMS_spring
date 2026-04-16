package vms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vms.dto.SignupRequest;
import vms.model.Role;
import vms.model.User;
import vms.repository.UserRepository;

import java.util.Optional;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // Sign up a new user
    public User signup(SignupRequest signupRequest) {
        // Check if username already exists
        if (userRepository.existsByUsername(signupRequest.username())) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(signupRequest.email())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        // Validate and convert role
        Role role;
        try {
            role = Role.valueOf(signupRequest.role().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role. Must be ADMIN or VOLUNTEER");
        }
        
        // Create new user
        User user = new User(
            signupRequest.username(),
            signupRequest.email(),
            passwordEncoder.encode(signupRequest.password()),
            role
        );
        
        return userRepository.save(user);
    }
    
    // Find user by username
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    // Verify password
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
