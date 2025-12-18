package edu.univ.erp.data;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * AuthDatabase - Handles all operations for the Authentication Database
 * Manages: users_auth table (user credentials, roles, and authentication data)
 * UPDATED: Added email field for password recovery
 * 
 * Database Schema:
 * users_auth(user_id, username, role, password_hash, email, status, last_login, failed_attempts, locked_until)
 */
public class AuthDatabase {
    
    private final Connection connection;
    
    // User roles
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_INSTRUCTOR = "INSTRUCTOR";
    public static final String ROLE_STUDENT = "STUDENT";
    
    // User status
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final String STATUS_LOCKED = "LOCKED";
    
    /**
     * Constructor - Initialize with an active database connection
     * @param connection Active JDBC connection to Auth DB
     */
    public AuthDatabase(Connection connection) {
        this.connection = connection;
    }
    


    public Connection getConnection() {
        return this.connection;
    }


    /**
     * Create the users_auth table if it doesn't exist
     * UPDATED: Added email field
     */
    public void createTablesIfNotExist() throws SQLException {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS users_auth (
                user_id INT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(50) UNIQUE NOT NULL,
                role ENUM('ADMIN', 'INSTRUCTOR', 'STUDENT') NOT NULL,
                password_hash VARCHAR(255) NOT NULL,
                email VARCHAR(100) UNIQUE,
                status ENUM('ACTIVE', 'INACTIVE', 'LOCKED') DEFAULT 'ACTIVE',
                last_login TIMESTAMP NULL,
                failed_attempts INT DEFAULT 0,
                locked_until TIMESTAMP NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                INDEX idx_username (username),
                INDEX idx_email (email),
                INDEX idx_role (role),
                INDEX idx_status (status)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
        }
        
        // Check if email column exists, if not add it (for existing databases)
        try {
            String checkColumnSQL = """
                SELECT COLUMN_NAME 
                FROM INFORMATION_SCHEMA.COLUMNS 
                WHERE TABLE_SCHEMA = DATABASE() 
                AND TABLE_NAME = 'users_auth' 
                AND COLUMN_NAME = 'email'
                """;
            
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(checkColumnSQL)) {
                
                if (!rs.next()) {
                    // Column doesn't exist, add it
                    String addColumnSQL = "ALTER TABLE users_auth ADD COLUMN email VARCHAR(100) UNIQUE AFTER password_hash";
                    try (Statement alterStmt = connection.createStatement()) {
                        alterStmt.execute(addColumnSQL);
                        System.out.println("Added email column to users_auth table");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Warning: Could not check/add email column: " + e.getMessage());
        }
    }
    
    /**
     * Add a new user to the authentication database
     * @param username Unique username
     * @param role User role (ADMIN, INSTRUCTOR, STUDENT)
     * @param passwordHash Hashed password (bcrypt)
     * @return user_id of newly created user
     */
    public int addUser(String username, String role, String passwordHash) throws SQLException {
        String sql = "INSERT INTO users_auth (username, role, password_hash) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, username);
            pstmt.setString(2, role);
            pstmt.setString(3, passwordHash);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        }
    }
    
    /**
     * Add a new user to the authentication database with email
     * @param username Unique username
     * @param role User role (ADMIN, INSTRUCTOR, STUDENT)
     * @param passwordHash Hashed password (bcrypt)
     * @param email User email address
     * @return user_id of newly created user
     */
    public int addUser(String username, String role, String passwordHash, String email) throws SQLException {
        String sql = "INSERT INTO users_auth (username, role, password_hash, email) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, username);
            pstmt.setString(2, role);
            pstmt.setString(3, passwordHash);
            pstmt.setString(4, email);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        }
    }
    
    /**
     * Find user by username
     * @param username Username to search
     * @return Optional UserAuth object
     */
    public Optional<UserAuth> findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users_auth WHERE username = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUserAuth(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Find user by email
     * @param email Email to search
     * @return Optional UserAuth object
     */
    public Optional<UserAuth> findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users_auth WHERE email = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUserAuth(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Find user by user ID
     * @param userId User ID to search
     * @return Optional UserAuth object
     */
    public Optional<UserAuth> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM users_auth WHERE user_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUserAuth(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Get all users by role
     * @param role Role to filter (ADMIN, INSTRUCTOR, STUDENT)
     * @return List of UserAuth objects
     */
    public List<UserAuth> getUsersByRole(String role) throws SQLException {
        List<UserAuth> users = new ArrayList<>();
        String sql = "SELECT * FROM users_auth WHERE role = ? ORDER BY username";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, role);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUserAuth(rs));
                }
            }
        }
        
        return users;
    }
    
    /**
     * Get all users
     * @return List of all UserAuth objects
     */
    public List<UserAuth> getAllUsers() throws SQLException {
        List<UserAuth> users = new ArrayList<>();
        String sql = "SELECT * FROM users_auth ORDER BY role, username";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                users.add(mapResultSetToUserAuth(rs));
            }
        }
        
        return users;
    }
    
    /**
     * Update user's password
     * @param userId User ID
     * @param newPasswordHash New hashed password
     */
    public void updatePassword(int userId, String newPasswordHash) throws SQLException {
        String sql = "UPDATE users_auth SET password_hash = ? WHERE user_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newPasswordHash);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Update user's email
     * @param userId User ID
     * @param email New email
     */
    public void updateEmail(int userId, String email) throws SQLException {
        String sql = "UPDATE users_auth SET email = ? WHERE user_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Update user's status
     * @param userId User ID
     * @param status New status (ACTIVE, INACTIVE, LOCKED)
     */
    public void updateStatus(int userId, String status) throws SQLException {
        String sql = "UPDATE users_auth SET status = ? WHERE user_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Update last login timestamp
     * @param userId User ID
     */
    public void updateLastLogin(int userId) throws SQLException {
        String sql = "UPDATE users_auth SET last_login = CURRENT_TIMESTAMP WHERE user_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Increment failed login attempts
     * @param userId User ID
     * @return New count of failed attempts
     */
    public int incrementFailedAttempts(int userId) throws SQLException {
        String sql = "UPDATE users_auth SET failed_attempts = failed_attempts + 1 WHERE user_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        }
        
        // Return new count
        String selectSql = "SELECT failed_attempts FROM users_auth WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(selectSql)) {
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("failed_attempts");
                }
            }
        }
        
        return 0;
    }
    
    /**
     * Reset failed login attempts to 0
     * @param userId User ID
     */
    public void resetFailedAttempts(int userId) throws SQLException {
        String sql = "UPDATE users_auth SET failed_attempts = 0 WHERE user_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Lock user account until specified time
     * @param userId User ID
     * @param durationMinutes Duration in minutes
     */
    public void lockAccount(int userId, int durationMinutes) throws SQLException {
        String sql = "UPDATE users_auth SET status = 'LOCKED', locked_until = DATE_ADD(NOW(), INTERVAL ? MINUTE) WHERE user_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, durationMinutes);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Unlock account if lock time has expired
     * @param userId User ID
     */
    public void unlockAccountIfExpired(int userId) throws SQLException {
        String sql = """
            UPDATE users_auth 
            SET status = 'ACTIVE', locked_until = NULL, failed_attempts = 0 
            WHERE user_id = ? AND locked_until IS NOT NULL AND locked_until < NOW()
            """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Check if username exists
     * @param username Username to check
     * @return true if exists
     */
    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users_auth WHERE username = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Check if email exists
     * @param email Email to check
     * @return true if exists
     */
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users_auth WHERE email = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Delete user by ID
     * @param userId User ID to delete
     */
    public void deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM users_auth WHERE user_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Map ResultSet to UserAuth object
     */
    private UserAuth mapResultSetToUserAuth(ResultSet rs) throws SQLException {
        UserAuth user = new UserAuth();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setRole(rs.getString("role"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setEmail(rs.getString("email"));
        user.setStatus(rs.getString("status"));
        
        Timestamp lastLogin = rs.getTimestamp("last_login");
        if (lastLogin != null) {
            user.setLastLogin(lastLogin.toLocalDateTime());
        }
        
        user.setFailedAttempts(rs.getInt("failed_attempts"));
        
        Timestamp lockedUntil = rs.getTimestamp("locked_until");
        if (lockedUntil != null) {
            user.setLockedUntil(lockedUntil.toLocalDateTime());
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return user;
    }
    
    /**
     * Inner class - UserAuth data model
     * UPDATED: Added email field
     */
    public static class UserAuth {
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
        private LocalDateTime updatedAt;
        
        // Constructors
        public UserAuth() {}
        
        public UserAuth(int userId, String username, String role, String status) {
            this.userId = userId;
            this.username = username;
            this.role = role;
            this.status = status;
        }
        
        // Getters and Setters
        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public String getPasswordHash() { return passwordHash; }
        public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public LocalDateTime getLastLogin() { return lastLogin; }
        public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
        
        public int getFailedAttempts() { return failedAttempts; }
        public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }
        
        public LocalDateTime getLockedUntil() { return lockedUntil; }
        public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
        
        // Utility methods
        public boolean isLocked() {
            return STATUS_LOCKED.equals(status) && 
                   lockedUntil != null && 
                   lockedUntil.isAfter(LocalDateTime.now());
        }
        
        public boolean isActive() {
            return STATUS_ACTIVE.equals(status);
        }
        
        @Override
        public String toString() {
            return String.format("UserAuth[id=%d, username=%s, role=%s, email=%s, status=%s]", 
                               userId, username, role, email, status);
        }
    }
}
