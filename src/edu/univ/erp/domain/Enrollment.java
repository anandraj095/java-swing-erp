package edu.univ.erp.domain;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

import edu.univ.erp.data.ERPDatabase;

/**
 * Enrollment Domain Model
 * Represents a student's enrollment in a section
 * Links students to sections with status tracking
 */
public class Enrollment {
    
    // Fields
    private int enrollmentId;
    private int studentId;
    private int sectionId;
    private String status;
    private LocalDateTime enrollmentDate;
    private LocalDateTime dropDate;
    private String finalGrade;
    
    // Additional fields from JOINs (for display purposes)
    private String sectionName;
    private String courseCode;
    private String courseTitle;
    private int credits;
    private String dayTime;
    private String room;
    private String semester;
    private int year;
    private String instructorName;
    private String studentRollNo;
    private String studentProgram;
    private int studentYear;
    private Student student;
    private String studentName;
    
    // Constants
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_DROPPED = "DROPPED";
    public static final String STATUS_COMPLETED = "COMPLETED";
    
    // Valid letter grades
    private static final String[] VALID_GRADES = {
        "A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", 
        "D+", "D", "F", "W", "I", "P", "NP"
    };
    
    // Constructors
    public Enrollment() {
        this.status = STATUS_ACTIVE;
        this.enrollmentDate = LocalDateTime.now();
    }
    
    public Enrollment(int studentId, int sectionId, ERPDatabase erpDb) {
        this();
        this.studentId = studentId;
        this.sectionId = sectionId;
        try {
        var studentOpt = erpDb.getStudentById(studentId);
        if (studentOpt.isEmpty()) {
            throw new RuntimeException("Student not found: " + studentId);
        }
        this.student = studentOpt.get();
        } catch (SQLException e) {
            throw new RuntimeException("Database error while fetching student", e);
        }
    }
    
    // Getters and Setters

    public String getStudentName() {
        if (studentName != null) {
            return studentName;
        }
        return student != null ? student.getFullName() : "";
    }
    
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return student.getEmail();
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }
    
    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }
    
    public int getStudentId() {
        return studentId;
    }
    
    public void setStudentId(int studentId) {
        if (studentId <= 0) {
            throw new IllegalArgumentException("Student ID must be positive");
        }
        this.studentId = studentId;
    }
    
    public int getSectionId() {
        return sectionId;
    }
    
    public void setSectionId(int sectionId) {
        if (sectionId <= 0) {
            throw new IllegalArgumentException("Section ID must be positive");
        }
        this.sectionId = sectionId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        if (!isValidStatus(status)) {
            throw new IllegalArgumentException("Invalid enrollment status: " + status);
        }
        this.status = status;
    }
    
    public LocalDateTime getEnrollmentDate() {
        return enrollmentDate;
    }
    
    public void setEnrollmentDate(LocalDateTime enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }
    
    public LocalDateTime getDropDate() {
        return dropDate;
    }
    
    public void setDropDate(LocalDateTime dropDate) {
        this.dropDate = dropDate;
    }
    
    public String getFinalGrade() {
        return finalGrade;
    }
    
    public void setFinalGrade(String finalGrade) {
        if (finalGrade != null && !finalGrade.trim().isEmpty()) {
            finalGrade = finalGrade.trim().toUpperCase();
            if (!isValidGrade(finalGrade)) {
                throw new IllegalArgumentException("Invalid grade: " + finalGrade);
            }
        }
        this.finalGrade = finalGrade;
    }
    
    // Additional fields from JOINs
    public String getSectionName() {
        return sectionName;
    }
    
    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }
    
    public String getCourseCode() {
        return courseCode;
    }
    
    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }
    
    public String getCourseTitle() {
        return courseTitle;
    }
    
    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }
    
    public int getCredits() {
        return credits;
    }
    
    public void setCredits(int credits) {
        this.credits = credits;
    }
    
    public String getDayTime() {
        return dayTime;
    }
    
    public void setDayTime(String dayTime) {
        this.dayTime = dayTime;
    }
    
    public String getRoom() {
        return room;
    }
    
    public void setRoom(String room) {
        this.room = room;
    }
    
    public String getSemester() {
        return semester;
    }
    
    public void setSemester(String semester) {
        this.semester = semester;
    }
    
    public int getYear() {
        return year;
    }
    
    public void setYear(int year) {
        this.year = year;
    }
    
    public String getInstructorName() {
        return instructorName;
    }
    
    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
    }
    
    public String getStudentRollNo() {
        return studentRollNo;
    }
    
    public void setStudentRollNo(String studentRollNo) {
        this.studentRollNo = studentRollNo;
    }
    
    public String getStudentProgram() {
        return studentProgram;
    }
    
    public void setStudentProgram(String studentProgram) {
        this.studentProgram = studentProgram;
    }
    
    public int getStudentYear() {
        return studentYear;
    }
    
    public void setStudentYear(int studentYear) {
        this.studentYear = studentYear;
    }
    
    // Business Logic Methods
    
    /**
     * Check if enrollment is active
     * @return true if status is ACTIVE
     */
    public boolean isActive() {
        return STATUS_ACTIVE.equals(status);
    }
    
    /**
     * Check if enrollment is dropped
     * @return true if status is DROPPED
     */
    public boolean isDropped() {
        return STATUS_DROPPED.equals(status);
    }
    
    /**
     * Check if enrollment is completed
     * @return true if status is COMPLETED
     */
    public boolean isCompleted() {
        return STATUS_COMPLETED.equals(status);
    }
    
    /**
     * Check if enrollment has a final grade
     * @return true if final grade is set
     */
    public boolean hasGrade() {
        return finalGrade != null && !finalGrade.isEmpty();
    }
    
    /**
     * Mark enrollment as dropped
     */
    public void drop() {
        this.status = STATUS_DROPPED;
        this.dropDate = LocalDateTime.now();
    }
    
    /**
     * Mark enrollment as completed with grade
     * @param grade Final grade
     */
    public void complete(String grade) {
        this.status = STATUS_COMPLETED;
        this.finalGrade = grade;
    }
    
    /**
     * Get display name for enrollment
     * @return Course code and section name
     */
    public String getDisplayName() {
        if (courseCode != null && sectionName != null) {
            return courseCode + " " + sectionName;
        }
        return "Enrollment #" + enrollmentId;
    }
    
    /**
     * Get full display with term
     * @return Complete enrollment information
     */
    public String getFullDisplayName() {
        StringBuilder sb = new StringBuilder();
        if (courseCode != null) {
            sb.append(courseCode);
            if (sectionName != null) {
                sb.append(" ").append(sectionName);
            }
            if (courseTitle != null) {
                sb.append(" - ").append(courseTitle);
            }
            if (semester != null && year > 0) {
                sb.append(" (").append(semester).append(" ").append(year).append(")");
            }
        }
        return sb.toString();
    }
    
    /**
     * Get enrollment status description
     * @return Status with additional info
     */
    public String getStatusDescription() {
        return switch (status) {
            case STATUS_ACTIVE -> "Currently Enrolled";
            case STATUS_DROPPED -> "Dropped" + (dropDate != null ? " on " + formatDate(dropDate) : "");
            case STATUS_COMPLETED -> "Completed" + (hasGrade() ? " - Grade: " + finalGrade : "");
            default -> status;
        };
    }
    
    /**
     * Get formatted enrollment date
     * @return Formatted date string
     */
    public String getFormattedEnrollmentDate() {
        return enrollmentDate != null ? formatDate(enrollmentDate) : "N/A";
    }
    
    /**
     * Get formatted drop date
     * @return Formatted date string
     */
    public String getFormattedDropDate() {
        return dropDate != null ? formatDate(dropDate) : "N/A";
    }
    
    /**
     * Format LocalDateTime to readable string
     * @param dateTime DateTime to format
     * @return Formatted string
     */
    private String formatDate(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        return dateTime.format(formatter);
    }
    
    /**
     * Check if grade is passing
     * @return true if grade is passing (not F, NP, or W)
     */
    public boolean isPassing() {
        if (finalGrade == null) return false;
        return !finalGrade.equals("F") && !finalGrade.equals("NP") && !finalGrade.equals("W");
    }
    
    /**
     * Get grade points for GPA calculation
     * @return Grade points (4.0 scale)
     */
    public double getGradePoints() {
        if (finalGrade == null) return 0.0;
        return switch (finalGrade) {
            case "A+", "A" -> 4.0;
            case "A-" -> 3.7;
            case "B+" -> 3.3;
            case "B" -> 3.0;
            case "B-" -> 2.7;
            case "C+" -> 2.3;
            case "C" -> 2.0;
            case "C-" -> 1.7;
            case "D+" -> 1.3;
            case "D" -> 1.0;
            case "F" -> 0.0;
            default -> 0.0; // W, I, P, NP don't count
        };
    }
    
    /**
     * Check if grade counts towards GPA
     * @return true if grade affects GPA
     */
    public boolean countsTowardsGPA() {
        if (finalGrade == null) return false;
        // W (Withdraw), I (Incomplete), P (Pass), NP (No Pass) don't count
        return !finalGrade.equals("W") && !finalGrade.equals("I") && 
               !finalGrade.equals("P") && !finalGrade.equals("NP");
    }
    
    /**
     * Validate all required fields are present
     * @return true if all required fields are valid
     */
    public boolean isValid() {
        return studentId > 0 &&
               sectionId > 0 &&
               status != null && isValidStatus(status);
    }
    
    // Validation Methods
    
    /**
     * Validate enrollment status
     * @param status Status to validate
     * @return true if valid
     */
    public static boolean isValidStatus(String status) {
        return STATUS_ACTIVE.equals(status) || 
               STATUS_DROPPED.equals(status) || 
               STATUS_COMPLETED.equals(status);
    }
    
    /**
     * Validate grade
     * @param grade Grade to validate
     * @return true if valid
     */
    public static boolean isValidGrade(String grade) {
        if (grade == null || grade.trim().isEmpty()) return true;
        for (String validGrade : VALID_GRADES) {
            if (validGrade.equals(grade.toUpperCase())) {
                return true;
            }
        }
        return false;
    }
    
    // Utility Methods
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Enrollment that = (Enrollment) o;
        return enrollmentId == that.enrollmentId &&
               studentId == that.studentId &&
               sectionId == that.sectionId;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(enrollmentId, studentId, sectionId);
    }
    
    @Override
    public String toString() {
        return "Enrollment{" +
                "enrollmentId=" + enrollmentId +
                ", studentId=" + studentId +
                ", sectionId=" + sectionId +
                ", courseCode='" + courseCode + '\'' +
                ", sectionName='" + sectionName + '\'' +
                ", status='" + status + '\'' +
                ", finalGrade='" + finalGrade + '\'' +
                ", enrollmentDate=" + enrollmentDate +
                '}';
    }
    
    /**
     * Create a copy of this enrollment
     * @return New Enrollment object with same values
     */
    public Enrollment copy() {
        Enrollment copy = new Enrollment();
        copy.enrollmentId = this.enrollmentId;
        copy.studentId = this.studentId;
        copy.sectionId = this.sectionId;
        copy.status = this.status;
        copy.enrollmentDate = this.enrollmentDate;
        copy.dropDate = this.dropDate;
        copy.finalGrade = this.finalGrade;
        copy.sectionName = this.sectionName;
        copy.courseCode = this.courseCode;
        copy.courseTitle = this.courseTitle;
        copy.credits = this.credits;
        copy.dayTime = this.dayTime;
        copy.room = this.room;
        copy.semester = this.semester;
        copy.year = this.year;
        copy.instructorName = this.instructorName;
        copy.studentRollNo = this.studentRollNo;
        copy.studentProgram = this.studentProgram;
        copy.studentYear = this.studentYear;
        return copy;
    }
}