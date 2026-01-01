package edu.univ.erp.service;

import edu.univ.erp.data.AuthDatabase;
import edu.univ.erp.data.AuthDatabase.UserAuth;
import edu.univ.erp.data.ERPDatabase;
import edu.univ.erp.data.ERPDatabase.*;
import edu.univ.erp.domain.*;

import java.sql.SQLException;
import java.util.List;

/**
 * AdminService - Handles all admin-related operations
 * UPDATED: Added complete update methods for all user types including first/last name
 * UPDATED: Added maintenance mode checks - admin operations are not blocked by maintenance mode
 * FIXED: Updated createSectionWithDeadline to accept LocalDateTime instead of LocalDate
 */
public class AdminService {
    
    private final AuthDatabase authDb;
    private final ERPDatabase erpDb;
    private final AuthService authService;
    private final AccessControlService accessControl;
    
    public AdminService(AuthDatabase authDb, ERPDatabase erpDb,
                       AuthService authService, AccessControlService accessControl) {
        this.authDb = authDb;
        this.erpDb = erpDb;
        this.authService = authService;
        this.accessControl = accessControl;
    }
    
    // ==================== USER MANAGEMENT ====================
    
    /**
     * Creates a new student user
     * NOTE: Admin operations are NOT blocked by maintenance mode
     */
    public ServiceResult createStudent(String username, String password, String rollNo,
                                      String firstName, String lastName,
                                      String program, int year, String email, 
                                      String phone, String address) {
        try {
            // Check if username exists
            if (authDb.usernameExists(username)) {
                return ServiceResult.error("Username already exists");
            }
            
            // Validate password
            if (!authService.isValidPassword(password)) {
                return ServiceResult.error("Password must be at least 8 characters with uppercase, lowercase, and digit");
            }
            
            // Hash password and create user in Auth DB with email
            String passwordHash = authService.hashPassword(password);
            int userId = authDb.addUser(username, AuthDatabase.ROLE_STUDENT, passwordHash, email);
            
            // Create student profile in ERP DB
            int studentId = erpDb.addStudent(userId, rollNo, firstName, lastName, program, year, email, phone, address);
            
            return ServiceResult.success("Student created successfully with Roll No: " + rollNo, studentId);
            
        } catch (SQLException e) {
            return ServiceResult.error("Database error: " + e.getMessage());
        }
    }
    
    /**
     * Creates a new instructor user
     * NOTE: Admin operations are NOT blocked by maintenance mode
     */
    public ServiceResult createInstructor(String username, String password, String employeeId,
                                         String firstName, String lastName,
                                         String department, String designation, String email,
                                         String phone, String officeRoom) {
        try {
            // Check if username exists
            if (authDb.usernameExists(username)) {
                return ServiceResult.error("Username already exists");
            }
            
            // Validate password
            if (!authService.isValidPassword(password)) {
                return ServiceResult.error("Password must be at least 8 characters with uppercase, lowercase, and digit");
            }
            
            // Hash password and create user in Auth DB with email
            String passwordHash = authService.hashPassword(password);
            int userId = authDb.addUser(username, AuthDatabase.ROLE_INSTRUCTOR, passwordHash, email);
            
            // Create instructor profile in ERP DB
            int instructorId = erpDb.addInstructor(userId, employeeId, firstName, lastName, 
                                                  department, designation, email, phone, officeRoom);
            
            return ServiceResult.success("Instructor created successfully with Employee ID: " + employeeId, 
                                       instructorId);
            
        } catch (SQLException e) {
            return ServiceResult.error("Database error: " + e.getMessage());
        }
    }
    
    /**
     * Creates a new admin user
     * NOTE: Admin operations are NOT blocked by maintenance mode
     */
    public ServiceResult createAdmin(String username, String password, String adminCode,
                                    String firstName, String lastName, String email,
                                    String phone, String department, String designation) {
        try {
            // Check if username exists
            if (authDb.usernameExists(username)) {
                return ServiceResult.error("Username already exists");
            }
            
            // Validate password
            if (!authService.isValidPassword(password)) {
                return ServiceResult.error("Password must be at least 8 characters with uppercase, lowercase, and digit");
            }
            
            // Hash password and create user in Auth DB with email
            String passwordHash = authService.hashPassword(password);
            int userId = authDb.addUser(username, AuthDatabase.ROLE_ADMIN, passwordHash, email);
            
            // Create admin profile in ERP DB
            int adminId = erpDb.addAdmin(userId, adminCode, firstName, lastName, email, phone, department, designation);
            
            return ServiceResult.success("Admin created successfully with Admin Code: " + adminCode, adminId);
            
        } catch (SQLException e) {
            return ServiceResult.error("Database error: " + e.getMessage());
        }
    }
    
    /**
     * Deactivates a user account
     * NOTE: Admin operations are NOT blocked by maintenance mode
     */
    public ServiceResult deactivateUser(int userId) {
        try {
            authDb.updateStatus(userId, AuthDatabase.STATUS_INACTIVE);
            return ServiceResult.success("User deactivated successfully");
        } catch (SQLException e) {
            return ServiceResult.error("Failed to deactivate user: " + e.getMessage());
        }
    }
    
    /**
     * Activates a user account
     * NOTE: Admin operations are NOT blocked by maintenance mode
     */
    public ServiceResult activateUser(int userId) {
        try {
            authDb.updateStatus(userId, AuthDatabase.STATUS_ACTIVE);
            return ServiceResult.success("User activated successfully");
        } catch (SQLException e) {
            return ServiceResult.error("Failed to activate user: " + e.getMessage());
        }
    }
    
    /**
     * Resets user password (admin function)
     * NOTE: Admin operations are NOT blocked by maintenance mode
     * Uses resetPassword which bypasses maintenance mode check
     */
    public ServiceResult resetUserPassword(int userId, String newPassword) {
        try {
            authService.resetPassword(userId, newPassword);
            return ServiceResult.success("Password reset successfully");
        } catch (Exception e) {
            return ServiceResult.error("Failed to reset password: " + e.getMessage());
        }
    }
    
    /**
     * Gets all users
     */
    public List<UserAuth> getAllUsers() throws SQLException {
        return authDb.getAllUsers();
    }
    
    /**
     * Gets users by role
     */
    public List<UserAuth> getUsersByRole(String role) throws SQLException {
        return authDb.getUsersByRole(role);
    }
    
    /**
     * Gets all admins
     */
    public List<Admin> getAllAdmins() throws SQLException {
        return erpDb.getAllAdmins();
    }
    
    /**
     * Gets admin by user ID
     */
    public Admin getAdminByUserId(int userId) throws SQLException {
        return erpDb.getAdminByUserId(userId).orElse(null);
    }
    
    /**
     * Updates admin information completely including first/last name
     * NEW METHOD for Edit User functionality
     * NOTE: Admin operations are NOT blocked by maintenance mode
     */
    public ServiceResult updateAdmin(int adminId, String firstName, String lastName,
                                            String email, String phone, 
                                            String department, String designation) {
        try {
            erpDb.updateAdmin(adminId, firstName, lastName, email, phone, department, designation);
            return ServiceResult.success("Admin profile updated successfully");
        } catch (SQLException e) {
            return ServiceResult.error("Failed to update admin: " + e.getMessage());
        }
    }
    
    // ==================== COURSE MANAGEMENT ====================
    
    /**
     * Creates a new course
     * NOTE: Admin operations are NOT blocked by maintenance mode
     */
    public ServiceResult createCourse(String courseCode, String title, int credits,
                                     String description, String prerequisite) {
        try {
            // Validate course code
            if (courseCode == null || courseCode.trim().isEmpty()) {
                return ServiceResult.error("Course code is required");
            }
            
            // Validate credits
            if (credits <= 0 || credits > 10) {
                return ServiceResult.error("Credits must be between 1 and 10");
            }
            
            // Check if course code already exists
            if (erpDb.getCourseByCode(courseCode).isPresent()) {
                return ServiceResult.error("Course code already exists");
            }
            
            int courseId = erpDb.addCourse(courseCode, title, credits, description, prerequisite);
            return ServiceResult.success("Course created successfully: " + courseCode, courseId);
            
        } catch (SQLException e) {
            return ServiceResult.error("Database error: " + e.getMessage());
        }
    }
    
    /**
     * Updates a course
     * NOTE: Admin operations are NOT blocked by maintenance mode
     */
    public ServiceResult updateCourse(int courseId, String title, int credits,
                                     String description, String prerequisite) {
        try {
            // Validate credits
            if (credits <= 0 || credits > 10) {
                return ServiceResult.error("Credits must be between 1 and 10");
            }
            
            erpDb.updateCourse(courseId, title, credits, description, prerequisite);
            return ServiceResult.success("Course updated successfully");
            
        } catch (SQLException e) {
            return ServiceResult.error("Failed to update course: " + e.getMessage());
        }
    }
    
    /**
     * Gets all courses
     */
    public List<Course> getAllCourses() throws SQLException {
        return erpDb.getAllCourses();
    }
    
    /**
     * Gets course by ID
     */
    public Course getCourseById(int courseId) throws SQLException {
        return erpDb.getCourseById(courseId).orElse(null);
    }
    
    // ==================== SECTION MANAGEMENT ====================
    
    /**
     * Creates a new section WITHOUT drop deadline (backward compatibility)
     * NOTE: Admin operations are NOT blocked by maintenance mode
     */
    public ServiceResult createSection(int courseId, Integer instructorId, String sectionName,
                                    String dayTime, String room, int capacity,
                                    String semester, int year) {
        return createSectionWithDeadline(courseId, instructorId, sectionName, dayTime, 
                                        room, capacity, semester, year, null);
    }

    /**
     * FIXED: Creates a new section WITH drop deadline support (now accepts LocalDateTime)
     * NOTE: Admin operations are NOT blocked by maintenance mode
     * @param dropDeadline LocalDateTime with both date and time, or null for no deadline
     */
    public ServiceResult createSectionWithDeadline(int courseId, Integer instructorId, String sectionName,
                                                String dayTime, String room, int capacity,
                                                String semester, int year, java.time.LocalDateTime dropDeadline) {
        try {
            // Validate capacity
            if (capacity <= 0 || capacity > 1000) {
                return ServiceResult.error("Capacity must be between 1 and 1000");
            }
            
            // Verify course exists
            if (erpDb.getCourseById(courseId).isEmpty()) {
                return ServiceResult.error("Course not found");
            }
            
            // Verify instructor exists if assigned
            if (instructorId != null && instructorId > 0) {
                if (erpDb.getInstructorById(instructorId).isEmpty()) {
                    return ServiceResult.error("Instructor not found");
                }
            }
            
            // Use new method that supports drop deadline with LocalDateTime
            int sectionId = erpDb.addSectionWithDeadline(courseId, instructorId, sectionName, dayTime,
                                                        room, capacity, semester, year, dropDeadline);
            
            String message = "Section created successfully";
            if (dropDeadline != null) {
                message += " with drop deadline: " + dropDeadline.toString();
            }
            
            return ServiceResult.success(message, sectionId);
            
        } catch (SQLException e) {
            return ServiceResult.error("Database error: " + e.getMessage());
        }
    }

    
    /**
     * Assigns an instructor to a section
     * NOTE: Admin operations are NOT blocked by maintenance mode
     */
    public ServiceResult assignInstructor(int sectionId, int instructorId) {
        try {
            // Verify section exists
            if (erpDb.getSectionById(sectionId).isEmpty()) {
                return ServiceResult.error("Section not found");
            }
            
            // Verify instructor exists
            if (erpDb.getInstructorById(instructorId).isEmpty()) {
                return ServiceResult.error("Instructor not found");
            }
            
            erpDb.assignInstructorToSection(sectionId, instructorId);
            return ServiceResult.success("Instructor assigned successfully");
            
        } catch (SQLException e) {
            return ServiceResult.error("Failed to assign instructor: " + e.getMessage());
        }
    }
    
    /**
     * Gets all sections for a semester
     */
    public List<Section> getSectionsBySemester(String semester, int year) throws SQLException {
        return erpDb.getSectionsBySemesterYear(semester, year);
    }
    
    // ==================== STUDENT & INSTRUCTOR MANAGEMENT ====================
    
    /**
     * Gets all students
     */
    public List<Student> getAllStudents() throws SQLException {
        return erpDb.getAllStudents();
    }
    
    /**
     * Gets all instructors
     */
    public List<Instructor> getAllInstructors() throws SQLException {
        return erpDb.getAllInstructors();
    }
    
    /**
     * Updates student profile completely including first/last name
     * NEW METHOD for Edit User functionality
     * NOTE: Admin operations are NOT blocked by maintenance mode
     */
    public ServiceResult updateStudent(int studentId, String firstName, String lastName,
                                              String program, int year,
                                              String email, String phone, String address) {
        try {
            erpDb.updateStudent(studentId, firstName, lastName, program, year, email, phone, address);
            return ServiceResult.success("Student profile updated successfully");
        } catch (SQLException e) {
            return ServiceResult.error("Failed to update student: " + e.getMessage());
        }
    }
    
    /**
     * Updates instructor profile completely including first/last name
     * NEW METHOD for Edit User functionality
     * NOTE: Admin operations are NOT blocked by maintenance mode
     */
    public ServiceResult updateInstructor(int instructorId, String firstName, String lastName,
                                                 String department, String designation,
                                                 String email, String phone, String officeRoom) {
        try {
            erpDb.updateInstructor(instructorId, firstName, lastName, department, 
                                          designation, email, phone, officeRoom);
            return ServiceResult.success("Instructor profile updated successfully");
        } catch (SQLException e) {
            return ServiceResult.error("Failed to update instructor: " + e.getMessage());
        }
    }
    
    // ==================== MAINTENANCE MODE ====================
    
    /**
     * Toggles maintenance mode
     * NOTE: Only admins can toggle maintenance mode
     */
    public ServiceResult toggleMaintenanceMode(boolean enable) {
        try {
            accessControl.setMaintenanceMode(enable);
            String status = enable ? "enabled" : "disabled";
            return ServiceResult.success("Maintenance mode " + status);
        } catch (SQLException e) {
            return ServiceResult.error("Failed to toggle maintenance mode: " + e.getMessage());
        }
    }
    
    /**
     * Gets current maintenance mode status
     */
    public boolean isMaintenanceMode() throws SQLException {
        return accessControl.isMaintenanceMode();
    }
}
