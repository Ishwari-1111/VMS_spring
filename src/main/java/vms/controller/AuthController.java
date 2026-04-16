package vms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vms.dto.AuthResponse;
import vms.dto.LoginRequest;
import vms.dto.SignupRequest;
import vms.model.User;
import vms.security.JwtTokenProvider;
import vms.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    // Sign up endpoint
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest signupRequest) {
        try {
            User user = userService.signup(signupRequest);
            String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole().toString());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(
                "User registered successfully",
                token,
                user.getUsername(),
                user.getEmail(),
                user.getRole().getDisplayName()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse("Error", null, null, null, e.getMessage()));
        }
    }
    
    // Login endpoint
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        var user = userService.findByUsername(loginRequest.username());
        
        if (user.isEmpty() || !userService.verifyPassword(loginRequest.password(), user.get().getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse("Error", null, null, null, "Invalid username or password"));
        }
        
        User authenticatedUser = user.get();
        String token = jwtTokenProvider.generateToken(authenticatedUser.getUsername(), authenticatedUser.getRole().toString());
        
        return ResponseEntity.ok(new AuthResponse(
            "Login successful",
            token,
            authenticatedUser.getUsername(),
            authenticatedUser.getEmail(),
            authenticatedUser.getRole().getDisplayName()
        ));
    }
    
    // Get current user info (protected endpoint)
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            if (!jwtTokenProvider.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthResponse("Error", null, null, null, "Invalid or expired token"));
            }
            
            String username = jwtTokenProvider.getUsernameFromToken(token);
            var user = userService.findByUsername(username);
            
            if (user.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new AuthResponse("Error", null, null, null, "User not found"));
            }
            
            User authenticatedUser = user.get();
            return ResponseEntity.ok(new AuthResponse(
                "User information",
                token,
                authenticatedUser.getUsername(),
                authenticatedUser.getEmail(),
                authenticatedUser.getRole().getDisplayName()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse("Error", null, null, null, "Invalid token"));
        }
    }
}
