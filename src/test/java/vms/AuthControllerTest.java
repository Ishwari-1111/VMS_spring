package vms;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import vms.controller.AuthController;
import vms.dto.AuthResponse;
import vms.dto.LoginRequest;
import vms.dto.SignupRequest;
import vms.model.Role;
import vms.model.User;
import vms.security.JwtTokenProvider;
import vms.service.UserService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testSignupSuccess() throws Exception {
        SignupRequest request = new SignupRequest("testuser", "test@example.com", "password", "VOLUNTEER");
        User user = new User("testuser", "test@example.com", "encodedPass", Role.VOLUNTEER);
        when(userService.signup(request)).thenReturn(user);
        when(jwtTokenProvider.generateToken(user.getUsername(), user.getRole().toString()))
                .thenReturn("dummy-token");
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.token").value("dummy-token"));
    }

    @Test
    void testSignupDuplicateUsername() throws Exception {
        SignupRequest request = new SignupRequest("dupUser", "dup@example.com", "password", "ADMIN");
        when(userService.signup(request)).thenThrow(new IllegalArgumentException("Username already exists"));
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error"));
    }

    @Test
    void testLoginSuccess() throws Exception {
        LoginRequest request = new LoginRequest("loginUser", "pass");
        User user = new User("loginUser", "login@example.com", "encodedPass", Role.ADMIN);
        when(userService.findByUsername("loginUser")).thenReturn(java.util.Optional.of(user));
        when(userService.verifyPassword("pass", user.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateToken(user.getUsername(), user.getRole().toString()))
                .thenReturn("login-token");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.token").value("login-token"));
    }

    @Test
    void testLoginInvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest("badUser", "badPass");
        when(userService.findByUsername("badUser")).thenReturn(java.util.Optional.empty());
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Error"));
    }

    @Test
    void testGetCurrentUserValidToken() throws Exception {
        String token = "Bearer valid-token";
        User user = new User("meUser", "me@example.com", "encoded", Role.VOLUNTEER);
        when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken("valid-token")).thenReturn("meUser");
        when(userService.findByUsername("meUser")).thenReturn(java.util.Optional.of(user));
        when(jwtTokenProvider.generateToken(user.getUsername(), user.getRole().toString()))
                .thenReturn("valid-token");
        mockMvc.perform(get("/api/auth/me").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("meUser"));
    }

    @Test
    void testGetCurrentUserInvalidToken() throws Exception {
        String token = "Bearer bad-token";
        when(jwtTokenProvider.validateToken("bad-token")).thenReturn(false);
        mockMvc.perform(get("/api/auth/me").header("Authorization", token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Error"));
    }

    @ParameterizedTest
    @CsvSource({
            "'', valid@example.com, password123, ADMIN",        // Blank username (Negative/Validation test)
            "ab, valid@example.com, password123, ADMIN",          // Short username (Negative/Boundary test)
            "validUser, invalid-email, password123, ADMIN",       // Invalid email format (Negative test)
            "validUser, valid@example.com, short, ADMIN",         // Short password (Negative/Boundary test)
            "validUser, valid@example.com, password123, ''"       // Blank role (Negative/Validation test)
    })
    void testSignupValidationFailures(String username, String email, String password, String role) throws Exception {
        // This parameterised test loops over the inputs above.
        // It acts as a comprehensive negative testing suite for input validation.
        SignupRequest request = new SignupRequest(username, email, password, role);
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // Expect Spring validation to reject invalid inputs
    }
}
