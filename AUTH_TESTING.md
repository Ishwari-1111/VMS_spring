# VMS Authentication Module - Testing Guide

## 📋 Overview

This guide explains how to test the RESTful Authentication API for the Volunteer Management System using Postman. The auth module provides role-based access control (RBAC) with two roles: **ADMIN** and **VOLUNTEER**.

---

## 🔧 Tech Stack Used

- **Spring Security 6** - Authentication & Authorization framework
- **JWT (JSON Web Tokens)** - Stateless authentication mechanism
- **JJWT Library (v0.12.3)** - Token generation, validation, and parsing
- **BCrypt** - Password hashing and validation
- **PostgreSQL** - User data persistence
- **Spring Data JPA** - Database operations and repositories

---

## 📁 Files Created/Modified

### **Created Files:**

| File                                                                                    | Purpose                                        |
| --------------------------------------------------------------------------------------- | ---------------------------------------------- |
| [User.java](src/main/java/vms/model/User.java)                                          | User entity with JPA annotations               |
| [Role.java](src/main/java/vms/model/Role.java)                                          | Enum for user roles (ADMIN, VOLUNTEER)         |
| [JwtTokenProvider.java](src/main/java/vms/security/JwtTokenProvider.java)               | JWT token generation & validation              |
| [JwtAuthenticationFilter.java](src/main/java/vms/security/JwtAuthenticationFilter.java) | Request filter for JWT validation              |
| [SecurityConfig.java](src/main/java/vms/security/SecurityConfig.java)                   | Spring Security configuration & RBAC rules     |
| [AuthController.java](src/main/java/vms/controller/AuthController.java)                 | REST endpoints for signup, login, current user |
| [UserService.java](src/main/java/vms/service/UserService.java)                          | Business logic for user management             |
| [UserRepository.java](src/main/java/vms/repository/UserRepository.java)                 | Database queries for User                      |
| [SignupRequest.java](src/main/java/vms/dto/SignupRequest.java)                          | Request DTO for signup                         |
| [LoginRequest.java](src/main/java/vms/dto/LoginRequest.java)                            | Request DTO for login                          |
| [AuthResponse.java](src/main/java/vms/dto/AuthResponse.java)                            | Response DTO for auth endpoints                |
| [ErrorResponse.java](src/main/java/vms/dto/ErrorResponse.java)                          | Response DTO for errors                        |

### **Modified Files:**

| File                                                                | Changes                                                   |
| ------------------------------------------------------------------- | --------------------------------------------------------- |
| [pom.xml](pom.xml)                                                  | Added Spring Security, JJWT, and PostgreSQL dependencies  |
| [application.properties](src/main/resources/application.properties) | Added JWT secret & expiration config, PostgreSQL settings |

---

## 🔑 Database Changes

### **New `users` Table**

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL, -- 'ADMIN' or 'VOLUNTEER'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

To create this table in Supabase, run the SQL above in their SQL Editor.

---

## 🚀 How to Test Auth APIs with Postman

### **Prerequisites:**

1. Install [Postman](https://www.postman.com/downloads/)
2. Spring Boot app running on `http://localhost:8080`
3. PostgreSQL/Supabase database configured

### **Step 1: Create Postman Environment Variables** (Optional but Recommended)

In Postman, create an environment called "VMS Auth" with these variables:

| Variable   | Value                                |
| ---------- | ------------------------------------ |
| `base_url` | `http://localhost:8080`              |
| `token`    | (Will be auto-populated after login) |
| `username` | (Will store last used username)      |

---

## 📡 API Endpoints & Testing

### **1️⃣ SIGN UP - Create New User**

**Endpoint:** `POST /api/auth/signup`

**Request Body (JSON):**

```json
{
  "username": "john_volunteer",
  "email": "john@example.com",
  "password": "password123",
  "role": "VOLUNTEER"
}
```

**Postman Setup:**

1. Create new request → POST
2. URL: `http://localhost:8080/api/auth/signup`
3. Headers: `Content-Type: application/json`
4. Body → raw → JSON → paste above JSON
5. Send

**Expected Response (201 Created):**

```json
{
  "message": "User registered successfully",
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huX3ZvbHVudGVlciIsInJvbGUiOiJWT0xVTlRFRSIsImlhdCI6MTcxMzE4Njk0MCwiZXhwIjoxNzEzMjczMzQwfQ.8Wx7...",
  "username": "john_volunteer",
  "email": "john@example.com",
  "role": "Volunteer"
}
```

**Validation Rules:**

- ✓ Username: 3-50 characters, unique
- ✓ Email: Valid format, unique
- ✓ Password: Minimum 6 characters
- ✓ Role: Must be "ADMIN" or "VOLUNTEER" (case-insensitive)

**Error Scenarios:**

| Error                    | Status | Reason                      |
| ------------------------ | ------ | --------------------------- |
| Username already exists  | 400    | Duplicate username          |
| Email already exists     | 400    | Duplicate email             |
| Invalid role             | 400    | Role not ADMIN or VOLUNTEER |
| Request validation fails | 400    | Missing/invalid fields      |

---

### **2️⃣ LOGIN - Authenticate User**

**Endpoint:** `POST /api/auth/login`

**Request Body (JSON):**

```json
{
  "username": "john_volunteer",
  "password": "password123"
}
```

**Postman Setup:**

1. Create new request → POST
2. URL: `http://localhost:8080/api/auth/login`
3. Headers: `Content-Type: application/json`
4. Body → raw → JSON → paste above JSON
5. Send

**Expected Response (200 OK):**

```json
{
  "message": "Login successful",
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huX3ZvbHVudGVlciIsInJvbGUiOiJWT0xVTlRFRSIsImlhdCI6MTcxMzE4Njk0MCwiZXhwIjoxNzEzMjczMzQwfQ.8Wx7...",
  "username": "john_volunteer",
  "email": "john@example.com",
  "role": "Volunteer"
}
```

**Save Token for Later Use:**

1. Copy the `token` value from response
2. Create environment variable `token` with this value
3. Or manually add to Authorization header

**Error Scenarios:**

| Error               | Status | Reason                  |
| ------------------- | ------ | ----------------------- |
| Invalid credentials | 401    | Wrong username/password |
| User not found      | 401    | Username doesn't exist  |

---

### **3️⃣ GET CURRENT USER - Retrieve User Info**

**Endpoint:** `GET /api/auth/me`

**Authorization:** Bearer Token Required ✅

**Postman Setup:**

1. Create new request → GET
2. URL: `http://localhost:8080/api/auth/me`
3. Headers:
   - `Content-Type: application/json`
   - `Authorization: Bearer YOUR_TOKEN_HERE`
4. Send

**Example Header:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huX3ZvbHVudGVlciIsInJvbGUiOiJWT0xVTlRFRSIsImlhdCI6MTcxMzE4Njk0MCwiZXhwIjoxNzEzMjczMzQwfQ.8Wx7...
```

**Expected Response (200 OK):**

```json
{
  "message": "User information",
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "john_volunteer",
  "email": "john@example.com",
  "role": "Volunteer"
}
```

**Error Scenarios:**

| Error          | Status | Reason                          |
| -------------- | ------ | ------------------------------- |
| Missing token  | 401    | No Authorization header         |
| Invalid token  | 401    | Malformed/expired token         |
| Token expired  | 401    | Token past expiration time      |
| User not found | 404    | User deleted after token issued |

---

## 🔐 Role-Based Access Control (RBAC) Testing

### **Admin-Only Endpoints** (Requires `role: ADMIN`)

**Create Event (POST /api/events)** - Admin Only

```json
Headers:
{
  "Authorization": "Bearer YOUR_ADMIN_TOKEN"
}

Body (JSON):
{
  "eventId": "E001",
  "eventName": "Beach Cleanup",
  "date": "2024-06-15"
}
```

**Update Event (PUT /api/events/{eventId})** - Admin Only
**Delete Event (DELETE /api/events/{eventId})** - Admin Only

**Test Case - Admin Can Create Event:**

1. Sign up as ADMIN
2. Copy admin token from response
3. POST to `/api/events` with token
4. Expected: 201 Created ✅

**Test Case - Volunteer Cannot Create Event:**

1. Sign up as VOLUNTEER
2. Copy volunteer token from response
3. POST to `/api/events` with token
4. Expected: 403 Forbidden ❌

---

### **Volunteer-Only Endpoints** (Requires `role: VOLUNTEER`)

**Enroll in Event (POST /api/volunteers/{volunteerId}/enroll)** - Volunteer Only

```json
Headers:
{
  "Authorization": "Bearer YOUR_VOLUNTEER_TOKEN"
}

Body (JSON):
{
  "eventId": "E001"
}
```

**Test Case - Volunteer Can Enroll:**

1. Sign up as VOLUNTEER
2. Copy volunteer token from response
3. POST to `/api/volunteers/{YOUR_ID}/enroll` with token
4. Expected: 200 OK ✅

**Test Case - Admin Cannot Enroll:**

1. Sign up as ADMIN
2. Copy admin token from response
3. POST to `/api/volunteers/{SOME_ID}/enroll` with token
4. Expected: 403 Forbidden ❌

---

### **Public Endpoints** (No Auth Required)

**Get All Events (GET /api/events)**
**Get Event by ID (GET /api/events/{eventId})**
**Get All Volunteers (GET /api/volunteers)**
**Get Volunteer by ID (GET /api/volunteers/{volunteerId})**

**Test Case:**

```bash
GET http://localhost:8080/api/events
# No Authorization header needed
# Expected: 200 OK ✅
```

---

## 📊 Complete Testing Workflow

### **Scenario 1: Sign Up & Login**

```
Step 1: Sign up as a Volunteer
POST /api/auth/signup
{
  "username": "alice",
  "email": "alice@example.com",
  "password": "password456",
  "role": "VOLUNTEER"
}
Response: Status 201, get token1

Step 2: Login with credentials
POST /api/auth/login
{
  "username": "alice",
  "password": "password456"
}
Response: Status 200, get token2 (should match token1)

Step 3: Get current user info
GET /api/auth/me
Headers: Authorization: Bearer token2
Response: Status 200, user details
```

### **Scenario 2: Admin Operations**

```
Step 1: Sign up as Admin
POST /api/auth/signup
{
  "username": "bob_admin",
  "email": "bob@example.com",
  "password": "admin123",
  "role": "ADMIN"
}
Response: Status 201, get admin_token

Step 2: Create an event (Admin only)
POST /api/events
Headers: Authorization: Bearer admin_token
{
  "eventId": "E002",
  "eventName": "Tree Planting",
  "date": "2024-07-20"
}
Response: Status 201, event created

Step 3: Volunteer cannot create event
POST /api/events
Headers: Authorization: Bearer volunteer_token
{...}
Response: Status 403, Forbidden
```

### **Scenario 3: Token Expiration**

```
Step 1: Login and get token
POST /api/auth/login
Response: Status 200, get token

Step 2: Wait for token expiration (default: 24 hours = 86400000ms)
For testing: Modify jwt.expiration in application.properties to smaller value

Step 3: Try to use expired token
GET /api/auth/me
Headers: Authorization: Bearer expired_token
Response: Status 401, Unauthorized
```

---

## 🛠️ Postman Collection Setup

### **Import Variables in Tests**

After signup/login, save token automatically:

**In Postman, go to Tests tab and add:**

```javascript
// Save token from response
var jsonData = pm.response.json();
pm.environment.set("token", jsonData.token);
pm.environment.set("username", jsonData.username);
```

### **Use Variables in Requests**

**Authorization Header:**

```
Bearer {{token}}
```

**URL:**

```
GET http://localhost:8080/api/auth/me
```

---

## ⚙️ Configuration Details

### **JWT Configuration (application.properties)**

```properties
# JWT secret - CHANGE THIS FOR PRODUCTION!
jwt.secret=your-secret-key-change-this-to-something-long-and-random-for-production

# Token expiration in milliseconds (86400000 = 24 hours)
jwt.expiration=86400000
```

**For Production:**

- Use a long, random string for `jwt.secret`
- Store it as an environment variable, not in code
- Use shorter expiration for sensitive operations
- Consider refresh tokens for longer sessions

### **SecurityConfig - Access Rules**

| Endpoint                        | Authentication | Role      |
| ------------------------------- | -------------- | --------- |
| `POST /api/auth/signup`         | ❌ No          | -         |
| `POST /api/auth/login`          | ❌ No          | -         |
| `GET /api/events`               | ❌ No          | -         |
| `GET /api/events/{id}`          | ❌ No          | -         |
| `POST /api/events`              | ✅ Yes         | ADMIN     |
| `PUT /api/events/{id}`          | ✅ Yes         | ADMIN     |
| `DELETE /api/events/{id}`       | ✅ Yes         | ADMIN     |
| `POST /api/volunteers/*/enroll` | ✅ Yes         | VOLUNTEER |
| `GET /api/auth/me`              | ✅ Yes         | Any       |

---

## 🐛 Troubleshooting

### **Error: Token validation failed**

- Ensure token is copied exactly (without extra spaces)
- Check token is not expired
- Verify JWT secret matches in application.properties

### **Error: 403 Forbidden**

- Ensure user has correct role for endpoint
- Check Authorization header format is `Bearer TOKEN`
- Admin endpoints require ADMIN role, not just authentication

### **Error: Invalid credentials (401)**

- Double-check username and password are correct
- Passwords are case-sensitive
- Ensure user exists in database

### **Error: Database connection refused**

- Verify PostgreSQL/Supabase is running
- Check connection string in application.properties
- Ensure username/password are correct

---

## 📝 Code Quality Notes

### **Password Security**

- Passwords are hashed using BCrypt (not plaintext)
- Comparison uses `passwordEncoder.matches()` for secure validation
- Never log or expose passwords

### **Token Security**

- Tokens are signed with HS256 algorithm
- Include username and role in token claims
- Tokens include expiration timestamp
- Validation checks signature and expiration

### **Input Validation**

- All DTOs use Jakarta Validation annotations
- Username: 3-50 characters, unique
- Email: Valid format, unique
- Password: Minimum 6 characters
- Role: Enum validation (ADMIN or VOLUNTEER)

### **Error Handling**

- Specific error messages for debugging
- Appropriate HTTP status codes
- No sensitive data in error responses

---

## 🚀 Next Steps

1. **Create Frontend:** Build React/Vue UI for signup/login forms
2. **Add Refresh Tokens:** Extend session without re-login
3. **Add Email Verification:** Confirm email before account activation
4. **Add Permission Checks:** Fine-grained permissions per role
5. **Add Audit Logging:** Log authentication events

---

## 📚 References

- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [JJWT Documentation](https://github.com/jwtk/jjwt)
- [JWT.io](https://jwt.io/) - Decode JWT tokens
- [Postman Documentation](https://learning.postman.com/)
