package edu.univ.erp.domain;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Student Domain Model
 * Represents a student in the ERP Database
 * Linked to UserAuth via userId
 */
public class Student {
    
    // Fields
    private int studentId;
    private int userId;
    private String rollNo;
    private String program;
    private int year;
    private String email;
    private String phone;
    private String address;
    private String firstName;
    private String lastName;

    
    // Pattern for email validation
    private static final Pattern EMAIL_PATTERN = 
    Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    // Pattern for phone validation (flexible format)
    private static final Pattern PHONE_PATTERN = 
    Pattern.compile("^[\\d\\s()+-]{7,20}$");
    
    // Constructors
    public Student() {
    }
    
    public Student(int userId, String rollNo, String program, int year) {
        this.userId = userId;
        this.rollNo = rollNo;
        this.program = program;
        this.year = year;
    }
    
    // Getters and Setters
    public int getStudentId() {
        return studentId;
    }
    
    public void setStudentId(int studentId) {
        this.studentId = studentId;
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
    
    /**
     * Get display name for student
     * @return Roll number with program
     */
    // public String getDisplayName() {
    //     return rollNo + " - " + program + " (Year " + year + ")";
    // }


    // @Override
    public String getDisplayName() {
        return getFullName() + " (" + rollNo + ")";
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
    
    public String getRollNo() {
        return rollNo;
    }
    
    public void setRollNo(String rollNo) {
        if (rollNo == null || rollNo.trim().isEmpty()) {
            throw new IllegalArgumentException("Roll number cannot be empty");
        }
        if (rollNo.length() > 20) {
            throw new IllegalArgumentException("Roll number cannot exceed 20 characters");
        }
        this.rollNo = rollNo.trim().toUpperCase();
    }
    
    public String getProgram() {
        return program;
    }
    
    public void setProgram(String program) {
        if (program == null || program.trim().isEmpty()) {
            throw new IllegalArgumentException("Program cannot be empty");
        }
        if (program.length() > 100) {
            throw new IllegalArgumentException("Program name cannot exceed 100 characters");
        }
        this.program = program.trim();
    }
    
    public int getYear() {
        return year;
    }
    
    public void setYear(int year) {
        if (year < 1 || year > 10) {
            throw new IllegalArgumentException("Year must be between 1 and 10");
        }
        this.year = year;
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
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        if (address != null && !address.trim().isEmpty()) {
            this.address = address.trim();
        } else {
            this.address = address;
        }
    }
    
    // Business Logic Methods
    
    /**
     * Check if student has complete contact information
     * @return true if email and phone are provided
     */
    public boolean hasCompleteContactInfo() {
        return email != null && !email.isEmpty() && 
               phone != null && !phone.isEmpty();
    }
    
    /**
     * Check if student is in final year (4th year for most programs)
     * @return true if year is 4 or higher
     */
    public boolean isFinalYear() {
        return year >= 4;
    }
    
    /**
     * Check if student is a freshman (1st year)
     * @return true if year is 1
     */
    public boolean isFreshman() {
        return year == 1;
    }
    
    /**
     * Get year level name
     * @return Year level as string
     */
    public String getYearLevel() {
        return switch (year) {
            case 1 -> "Freshman";
            case 2 -> "Sophomore";
            case 3 -> "Junior";
            case 4 -> "Senior";
            default -> "Year " + year;
        };
    }
    
    
    /**
     * Validate all required fields are present
     * @return true if all required fields are valid
     */
    public boolean isValid() {
        return userId > 0 && 
               rollNo != null && !rollNo.isEmpty() &&
               program != null && !program.isEmpty() &&
               year >= 1 && year <= 10;
    }
    
    // Utility Methods
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return studentId == student.studentId && 
               userId == student.userId &&
               Objects.equals(rollNo, student.rollNo);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(studentId, userId, rollNo);
    }
    
    @Override
    public String toString() {
        return "Student{" +
                "studentId=" + studentId +
                ", userId=" + userId +
                ", rollNo='" + rollNo + '\'' +
                ", program='" + program + '\'' +
                ", year=" + year +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
    
    /**
     * Create a copy of this student
     * @return New Student object with same values
     */
    public Student copy() {
        Student copy = new Student();
        copy.studentId = this.studentId;
        copy.userId = this.userId;
        copy.rollNo = this.rollNo;
        copy.program = this.program;
        copy.year = this.year;
        copy.email = this.email;
        copy.phone = this.phone;
        copy.address = this.address;
        return copy;
    }
}