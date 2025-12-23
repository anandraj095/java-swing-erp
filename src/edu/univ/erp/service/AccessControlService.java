package edu.univ.erp.service;

import edu.univ.erp.data.ERPDatabase;

import java.sql.SQLException;

/**
 * AccessControlService - Handles access control and maintenance mode checks
 */
public class AccessControlService {
    
    private final ERPDatabase erpDb;
    private Boolean maintenanceModeCache = null;
    
    public AccessControlService(ERPDatabase erpDb) {
        this.erpDb = erpDb;
    }
    
    /**
     * Checks if maintenance mode is enabled
     */
    public boolean isMaintenanceMode() throws SQLException {
        if (maintenanceModeCache == null) {
            refreshMaintenanceMode();
        }
        return maintenanceModeCache;
    }
    
    /**
     * Refreshes maintenance mode from database
     */
    public void refreshMaintenanceMode() throws SQLException {
        maintenanceModeCache = erpDb.isMaintenanceMode();
    }
    
    /**
     * Sets maintenance mode (admin only)
     */
    public void setMaintenanceMode(boolean enabled) throws SQLException {
        erpDb.setMaintenanceMode(enabled);
        maintenanceModeCache = enabled;
    }
    
    /**
     * Checks if user can perform write operations
     * Admins can always write, others cannot write in maintenance mode
     */
    public boolean canWrite(String role) throws SQLException {
        // Admins can always write
        if ("ADMIN".equalsIgnoreCase(role)) {
            return true;
        }
        
        // Others cannot write in maintenance mode
        return !isMaintenanceMode();
    }
    
    /**
     * Validates if an operation is allowed
     * @param role User's role
     * @param isWriteOperation Whether this is a write operation
     * @return null if allowed, error message otherwise
     */
    public String validateOperation(String role, boolean isWriteOperation) {
        try {
            if (isWriteOperation && !canWrite(role)) {
                return "Operation blocked: System is in maintenance mode. Only viewing is allowed.";
            }
            return null; // Operation allowed
        } catch (SQLException e) {
            return "Error checking permissions: " + e.getMessage();
        }
    }
    
    /**
     * Checks if user can access student data
     */
    public boolean canAccessStudentData(int currentUserId, String currentRole, int targetStudentUserId) {
        // Admins can access all data
        if ("ADMIN".equalsIgnoreCase(currentRole)) {
            return true;
        }
        
        // Students can only access their own data
        if ("STUDENT".equalsIgnoreCase(currentRole)) {
            return currentUserId == targetStudentUserId;
        }
        
        // Instructors cannot access student personal data directly
        return false;
    }
    
    /**
     * Checks if instructor can access section data
     */
    public boolean canAccessSection(int currentInstructorId, int sectionInstructorId, String role) {
        // Admins can access all sections
        if ("ADMIN".equalsIgnoreCase(role)) {
            return true;
        }
        
        // Instructors can only access their own sections
        if ("INSTRUCTOR".equalsIgnoreCase(role)) {
            return currentInstructorId == sectionInstructorId;
        }
        
        return false;
    }
    
    /**
     * Checks if user has a specific role
     */
    public boolean hasRole(String userRole, String requiredRole) {
        return userRole != null && userRole.equalsIgnoreCase(requiredRole);
    }
}
