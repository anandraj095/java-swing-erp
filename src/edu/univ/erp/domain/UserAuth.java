package edu.univ.erp.domain;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * UserAuth Domain Model
 * Represents a user authentication record in the Auth Database
 * Contains credentials, role information, and security tracking
 */
public class UserAuth {
    
    // Fields
    private int userId;
    private String username;
    private String role;
    private String passwordHash;
    private String email;
    private String status;
    private LocalDateTime lastLogin;
    private int failedAttempts;
    private LocalDateTime lockedUntil;
    private LocalDateTime createdAt;
    
    // Constants for roles
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_INSTRUCTOR = "INSTRUCTOR";
    public static final String ROLE_STUDENT = "STUDENT";
    
    // Constants for status
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final String STATUS_LOCKED = "LOCKED";
    
    // Constructors
    public UserAuth() {
        this.status = STATUS_ACTIVE;
        this.failedAttempts = 0;
    }
    
    public UserAuth(String username, String role, String passwordHash) {
        this();
        this.username = username;
        this.role = role;
        this.passwordHash = passwordHash;
    }
    
    public UserAuth(String username, String role, String passwordHash, String email) {
        this();
        this.username = username;
        this.role = role;
        this.passwordHash = passwordHash;
        this.email = email;
    }
    
    // Getters and Setters
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (username.length() > 50) {
            throw new IllegalArgumentException("Username cannot exceed 50 characters");
        }
        this.username = username.trim();
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        if (!isValidRole(role)) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
        this.role = role;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.trim().isEmpty()) {
            throw new IllegalArgumentException("Password hash cannot be empty");
        }
        this.passwordHash = passwordHash;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        if (!isValidStatus(status)) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
        this.status = status;
    }
    
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    public int getFailedAttempts() {
        return failedAttempts;
    }
    
    public void setFailedAttempts(int failedAttempts) {
        if (failedAttempts < 0) {
            throw new IllegalArgumentException("Failed attempts cannot be negative");
        }
        this.failedAttempts = failedAttempts;
    }
    
    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }
    
    public void setLockedUntil(LocalDateTime lockedUntil) {
        this.lockedUntil = lockedUntil;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // Business Logic Methods
    
    /**
     * Check if the account is currently locked
     * @return true if locked and lock period hasn't expired
     */
    public boolean isLocked() {
        return STATUS_LOCKED.equals(status) && 
               lockedUntil != null && 
               LocalDateTime.now().isBefore(lockedUntil);
    }
    
    /**
     * Check if the account is active and not locked
     * @return true if active and not locked
     */
    public boolean isActive() {
        return STATUS_ACTIVE.equals(status) && !isLocked();
    }
    
    /**
     * Check if the account status has expired lock period
     * @return true if lock has expired
     */
    public boolean isLockExpired() {
        return STATUS_LOCKED.equals(status) && 
               lockedUntil != null && 
               LocalDateTime.now().isAfter(lockedUntil);
    }
    
    /**
     * Increment failed login attempts
     */
    public void incrementFailedAttempts() {
        this.failedAttempts++;
    }
    
    /**
     * Reset failed login attempts
     */
    public void resetFailedAttempts() {
        this.failedAttempts = 0;
    }
    
    /**
     * Check if user has a specific role
     * @param role Role to check
     * @return true if user has the role
     */
    public boolean hasRole(String role) {
        return this.role != null && this.role.equals(role);
    }
    
    /**
     * Check if user is admin
     * @return true if user is admin
     */
    public boolean isAdmin() {
        return hasRole(ROLE_ADMIN);
    }
    
    /**
     * Check if user is instructor
     * @return true if user is instructor
     */
    public boolean isInstructor() {
        return hasRole(ROLE_INSTRUCTOR);
    }
    
    /**
     * Check if user is student
     * @return true if user is student
     */
    public boolean isStudent() {
        return hasRole(ROLE_STUDENT);
    }
    
    // Validation Methods
    
    /**
     * Validate if role is valid
     * @param role Role to validate
     * @return true if valid
     */
    public static boolean isValidRole(String role) {
        return ROLE_ADMIN.equals(role) || 
               ROLE_INSTRUCTOR.equals(role) || 
               ROLE_STUDENT.equals(role);
    }
    
    /**
     * Validate if status is valid
     * @param status Status to validate
     * @return true if valid
     */
    public static boolean isValidStatus(String status) {
        return STATUS_ACTIVE.equals(status) || 
               STATUS_INACTIVE.equals(status) || 
               STATUS_LOCKED.equals(status);
    }
    
    // Utility Methods
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAuth userAuth = (UserAuth) o;
        return userId == userAuth.userId && 
               Objects.equals(username, userAuth.username);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId, username);
    }
    
    @Override
    public String toString() {
        return "UserAuth{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", email='" + email + '\'' +
                ", status='" + status + '\'' +
                ", lastLogin=" + lastLogin +
                ", failedAttempts=" + failedAttempts +
                ", isLocked=" + isLocked() +
                '}';
    }
    
    /**
     * Get a display-friendly representation
     * @return Display string
     */
    public String getDisplayName() {
        return username + " (" + role + ")";
    }
}
