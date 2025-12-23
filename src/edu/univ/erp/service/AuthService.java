package edu.univ.erp.service;

import edu.univ.erp.data.AuthDatabase;
import edu.univ.erp.data.AuthDatabase.UserAuth;
import edu.univ.erp.service.AuthService.AuthException;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;

/**
 * AuthService - Authentication and Authorization Service
 * UPDATED: Added autoLogin method for Remember Me functionality
 * UPDATED: Added maintenance mode check for password changes
 */
public class AuthService {

    private final AuthDatabase authDb;
    private AccessControlService accessControl;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 10;

    public AuthService(AuthDatabase authDb) {
        this.authDb = authDb;
    }

    /**
     * Set access control service for maintenance mode checks
     */
    public void setAccessControl(AccessControlService accessControl) {
        this.accessControl = accessControl;
    }

    
    public static class LoginResult {
        private boolean success;
        private String role;
        private UserAuth user;
        private String message;

        public LoginResult(boolean success, String role, UserAuth user, String message) {
            this.success = success;
            this.role = role;
            this.user = user;
            this.message = message;
        }

        // UI EXPECTED METHODS
        public boolean isSuccess() {
            return success;
        }

        public String getRole() {
            return role;
        }

        public UserAuth getUser() {
            return user;
        }

        public String getMessage() {
            return message;
        }
    }




    // ==========================================================
    // INTERNAL LOGIN CORE (returns UserAuth)
    // ==========================================================
    private UserAuth loginCore(String username, String password)
            throws SQLException, AuthException {

        Optional<UserAuth> userOpt = authDb.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new AuthException("User not found.");
        }

        UserAuth user = userOpt.get();

        if (user.isLocked()) {
            authDb.unlockAccountIfExpired(user.getUserId());
            user = authDb.findByUserId(user.getUserId())
                    .orElseThrow(() -> new AuthException("Error refreshing user state."));
        }

        if (user.isLocked()) {
            throw new AuthException("Account locked until " + user.getLockedUntil());
        }

        if (!verifyPassword(password, user.getPasswordHash())) {
            int fails = authDb.incrementFailedAttempts(user.getUserId());

            if (fails >= MAX_FAILED_ATTEMPTS) {
                authDb.lockAccount(user.getUserId(), LOCK_DURATION_MINUTES);
                throw new AuthException("Too many failed attempts. Locked for "
                        + LOCK_DURATION_MINUTES + " minutes.");
            }

            throw new AuthException("Incorrect password. Attempts left: "
                    + (MAX_FAILED_ATTEMPTS - fails));
        }

        authDb.updateLastLogin(user.getUserId());

        return authDb.findByUserId(user.getUserId())
                .orElseThrow(() -> new AuthException("Unexpected error after login."));
    }

    // ==========================================================
    // PUBLIC LOGIN FUNCTION (UI expects LoginResult)
    // ==========================================================
    public LoginResult login(String username, String password)
            throws SQLException {

        try {
            UserAuth user = loginCore(username, password);
            return new LoginResult(true, user.getRole(), user, "Login Successful");

        } catch (AuthException ex) {
            return new LoginResult(false, null, null, ex.getMessage());
        }
    }

    // ==========================================================
    // AUTO LOGIN (for Remember Me functionality)
    // ==========================================================
    /**
     * Auto-login using stored user ID (Remember Me feature)
     * @param userId User ID to auto-login
     * @return LoginResult with success status
     * @throws SQLException if database error occurs
     */
    public LoginResult autoLogin(int userId) throws SQLException {
        try {
            Optional<UserAuth> userOpt = authDb.findByUserId(userId);
            if (userOpt.isEmpty()) {
                throw new AuthException("User not found.");
            }

            UserAuth user = userOpt.get();

            // Check if account is locked
            if (user.isLocked()) {
                authDb.unlockAccountIfExpired(user.getUserId());
                user = authDb.findByUserId(user.getUserId())
                        .orElseThrow(() -> new AuthException("Error refreshing user state."));
            }

            if (user.isLocked()) {
                throw new AuthException("Account is locked.");
            }

            // Check if account is active
            if (!AuthDatabase.STATUS_ACTIVE.equals(user.getStatus())) {
                throw new AuthException("Account is not active.");
            }

            // Update last login
            authDb.updateLastLogin(user.getUserId());

            return new LoginResult(true, user.getRole(), user, "Auto-login Successful");

        } catch (AuthException ex) {
            return new LoginResult(false, null, null, ex.getMessage());
        }
    }

    // ==========================================================
    // UI also calls loginWithResult sometimes, keep it
    // ==========================================================
    public LoginResult loginWithResult(String username, String password)
            throws SQLException, AuthException {

        UserAuth user = loginCore(username, password);
        return new LoginResult(true, user.getRole(), user, "Login Successful");
    }


    // ==========================================================
    // REGISTRATION
    // ==========================================================
    public int register(String username, String role, String plainPassword)
            throws SQLException, AuthException {

        if (username == null || username.isBlank())
            throw new AuthException("Username cannot be empty.");

        if (role == null || role.isBlank())
            throw new AuthException("Role cannot be empty.");

        if (plainPassword == null || plainPassword.isBlank())
            throw new AuthException("Password cannot be empty.");

        if (authDb.usernameExists(username))
            throw new AuthException("Username already exists.");

        String hash = hashPassword(plainPassword);

        return authDb.addUser(username, role, hash);
    }

    // ==========================================================
    // CHANGE PASSWORD
    // UPDATED: Added maintenance mode check for non-admin users
    // ==========================================================
    public void changePassword(int userId, String newPassword)
            throws SQLException, AuthException {

        if (newPassword == null || newPassword.isBlank())
            throw new AuthException("Password cannot be empty.");

        // Check maintenance mode for non-admin users
        if (accessControl != null) {
            Optional<UserAuth> userOpt = authDb.findByUserId(userId);
            if (userOpt.isPresent()) {
                String userRole = userOpt.get().getRole();
                String validationError = accessControl.validateOperation(userRole, true);
                if (validationError != null) {
                    throw new AuthException(validationError);
                }
            }
        }

        String hash = hashPassword(newPassword);
        authDb.updatePassword(userId, hash);
    }

    // ==========================================================
    // RESET PASSWORD (required by AdminService)
    // Admins can reset passwords even in maintenance mode
    // ==========================================================
    public void resetPassword(int userId, String newPassword)
            throws SQLException, AuthException {
        
        if (newPassword == null || newPassword.isBlank())
            throw new AuthException("Password cannot be empty.");

        String hash = hashPassword(newPassword);
        authDb.updatePassword(userId, hash);
    }

    // ==========================================================
    // ADMIN HELPERS
    // ==========================================================
    public void activateUser(int userId) throws SQLException {
        authDb.updateStatus(userId, AuthDatabase.STATUS_ACTIVE);
    }

    public void deactivateUser(int userId) throws SQLException {
        authDb.updateStatus(userId, AuthDatabase.STATUS_INACTIVE);
    }

    public void lockUser(int userId, int minutes) throws SQLException {
        authDb.lockAccount(userId, minutes);
    }

    public void unlockUser(int userId) throws SQLException {
        authDb.updateStatus(userId, AuthDatabase.STATUS_ACTIVE);

        String sqlAttempts = "UPDATE users_auth SET failed_attempts = 0 WHERE user_id = ?";
        try (PreparedStatement pstmt = authDb.getConnection().prepareStatement(sqlAttempts)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        }

        String sqlUnlock = "UPDATE users_auth SET locked_until = NULL WHERE user_id = ?";
        try (PreparedStatement pstmt = authDb.getConnection().prepareStatement(sqlUnlock)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        }
    }

    // ==========================================================
    // PASSWORD UTILS
    // ==========================================================
    public boolean isValidPassword(String pwd) {
        return pwd != null && !pwd.isBlank() && pwd.length() >= 4;
    }

    private boolean verifyPassword(String input, String stored) {
        if (stored == null) return false;
        return BCrypt.checkpw(input, stored);
    }

    // UI expects this to be public
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    // ==========================================================
    // USER RETRIEVAL
    // ==========================================================
    public Optional<UserAuth> getUserById(int userId) throws SQLException {
        return authDb.findByUserId(userId);
    }

    public Optional<UserAuth> getUserByUsername(String username) throws SQLException {
        return authDb.findByUsername(username);
    }

    // ==========================================================
    // LOGOUT
    // ==========================================================
    public void logout(int userId) {
        System.out.println("User " + userId + " logged out.");
    }

    // overload for UI compatibility
    public void logout() {
        System.out.println("User logged out.");
    }

    // ==========================================================
    // CUSTOM AUTH EXCEPTION
    // ==========================================================
    public static class AuthException extends Exception {
        public AuthException(String msg) { super(msg); }
    }
}
