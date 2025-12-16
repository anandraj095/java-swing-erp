package edu.univ.erp.domain;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Instructor Domain Model
 * Represents an instructor in the ERP Database
 * Linked to UserAuth via userId
 */
public class Instructor {
    
    // Fields
    private int instructorId;
    private int userId;
    private String employeeId;
    private String department;
    private String designation;
    private String email;
    private String phone;
    private String officeRoom;
    private String firstName;
    private String lastName;
    
    // Pattern for email validation
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    // Pattern for phone validation
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^[\\d\\s()+-]{7,20}$");
    
    // Constructors
    public Instructor() {
    }
    
    public Instructor(int userId, String employeeId, String department) {
        this.userId = userId;
        this.employeeId = employeeId;
        this.department = department;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be empty");
        }
        if (firstName.length() > 100) {
            throw new IllegalArgumentException("First name cannot exceed 100 characters");
        }
        this.firstName = firstName.trim();
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be empty");
        }
        if (lastName.length() > 100) {
            throw new IllegalArgumentException("Last name cannot exceed 100 characters");
        }
        this.lastName = lastName.trim();
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    // Getters and Setters
    public int getInstructorId() {
        return instructorId;
    }
    
    public void setInstructorId(int instructorId) {
        this.instructorId = instructorId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        this.userId = userId;
    }
    
    public String getEmployeeId() {
        return employeeId;
    }
    
    public void setEmployeeId(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee ID cannot be empty");
        }
        if (employeeId.length() > 20) {
            throw new IllegalArgumentException("Employee ID cannot exceed 20 characters");
        }
        this.employeeId = employeeId.trim().toUpperCase();
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        if (department == null || department.trim().isEmpty()) {
            throw new IllegalArgumentException("Department cannot be empty");
        }
        if (department.length() > 100) {
            throw new IllegalArgumentException("Department name cannot exceed 100 characters");
        }
        this.department = department.trim();
    }
    
    public String getDesignation() {
        return designation;
    }
    
    public void setDesignation(String designation) {
        if (designation != null && !designation.trim().isEmpty()) {
            if (designation.length() > 50) {
                throw new IllegalArgumentException("Designation cannot exceed 50 characters");
            }
            this.designation = designation.trim();
        } else {
            this.designation = designation;
        }
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        if (email != null && !email.trim().isEmpty()) {
            email = email.trim().toLowerCase();
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                throw new IllegalArgumentException("Invalid email format: " + email);
            }
            if (email.length() > 100) {
                throw new IllegalArgumentException("Email cannot exceed 100 characters");
            }
        }
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        if (phone != null && !phone.trim().isEmpty()) {
            phone = phone.trim();
            if (!PHONE_PATTERN.matcher(phone).matches()) {
                throw new IllegalArgumentException("Invalid phone format: " + phone);
            }
            if (phone.length() > 20) {
                throw new IllegalArgumentException("Phone cannot exceed 20 characters");
            }
        }
        this.phone = phone;
    }
    
    public String getOfficeRoom() {
        return officeRoom;
    }
    
    public void setOfficeRoom(String officeRoom) {
        if (officeRoom != null && !officeRoom.trim().isEmpty()) {
            if (officeRoom.length() > 50) {
                throw new IllegalArgumentException("Office room cannot exceed 50 characters");
            }
            this.officeRoom = officeRoom.trim();
        } else {
            this.officeRoom = officeRoom;
        }
    }
    
    // Business Logic Methods
    
    /**
     * Check if instructor has complete contact information
     * @return true if email and phone are provided
     */
    public boolean hasCompleteContactInfo() {
        return email != null && !email.isEmpty() && 
               phone != null && !phone.isEmpty();
    }
    
    /**
     * Check if instructor has office assigned
     * @return true if office room is provided
     */
    public boolean hasOfficeAssigned() {
        return officeRoom != null && !officeRoom.isEmpty();
    }
    
    /**
     * Get full title with designation and department
     * @return Full title string
     */
    public String getFullTitle() {
        if (designation != null && !designation.isEmpty()) {
            return designation + ", " + department;
        }
        return department;
    }
    
    /**
     * Get display name for instructor
     * @return Employee ID with department
     */
    public String getDisplayName() {
        return employeeId + " - " + department;
    }
    
    /**
     * Get display name with designation
     * @return Full display name
     */
    public String getFullDisplayName() {
        if (designation != null && !designation.isEmpty()) {
            return employeeId + " - " + designation + " (" + department + ")";
        }
        return getDisplayName();
    }
    
    /**
     * Validate all required fields are present
     * @return true if all required fields are valid
     */
    public boolean isValid() {
        return userId > 0 && 
               employeeId != null && !employeeId.isEmpty() &&
               department != null && !department.isEmpty();
    }
    
    /**
     * Check if instructor is from a specific department
     * @param dept Department to check
     * @return true if matches
     */
    public boolean isFromDepartment(String dept) {
        return department != null && department.equalsIgnoreCase(dept);
    }
    
    /**
     * Check if instructor has a specific designation
     * @param desig Designation to check
     * @return true if matches
     */
    public boolean hasDesignation(String desig) {
        return designation != null && designation.equalsIgnoreCase(desig);
    }
    
    // Utility Methods
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Instructor that = (Instructor) o;
        return instructorId == that.instructorId && 
               userId == that.userId &&
               Objects.equals(employeeId, that.employeeId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(instructorId, userId, employeeId);
    }
    
    @Override
    public String toString() {
        return "Instructor{" +
                "instructorId=" + instructorId +
                ", userId=" + userId +
                ", employeeId='" + employeeId + '\'' +
                ", department='" + department + '\'' +
                ", designation='" + designation + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", officeRoom='" + officeRoom + '\'' +
                '}';
    }
    
    /**
     * Create a copy of this instructor
     * @return New Instructor object with same values
     */
    public Instructor copy() {
        Instructor copy = new Instructor();
        copy.instructorId = this.instructorId;
        copy.userId = this.userId;
        copy.employeeId = this.employeeId;
        copy.department = this.department;
        copy.designation = this.designation;
        copy.email = this.email;
        copy.phone = this.phone;
        copy.officeRoom = this.officeRoom;
        return copy;
    }
}