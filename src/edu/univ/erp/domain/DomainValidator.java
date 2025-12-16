package edu.univ.erp.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

/**
 * DomainValidator - Utility class for common validation operations
 * Provides reusable validation methods for domain models
 */
public class DomainValidator {
    
    // Email pattern
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    // Phone pattern (flexible format)
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^[\\d\\s()+-]{7,20}$");
    
    // Course code pattern (e.g., CS101, MATH201)
    private static final Pattern COURSE_CODE_PATTERN = 
        Pattern.compile("^[A-Z]{2,4}\\d{3,4}$");
    
    // Roll number pattern (flexible)
    private static final Pattern ROLL_NO_PATTERN = 
        Pattern.compile("^[A-Z0-9]{5,20}$");
    
    // Employee ID pattern
    private static final Pattern EMPLOYEE_ID_PATTERN = 
        Pattern.compile("^[A-Z0-9]{3,20}$");
    
    // Private constructor to prevent instantiation
    private DomainValidator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    // ==================== String Validation ====================
    
    /**
     * Validate if string is not null and not empty
     * @param value String to validate
     * @return true if valid
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
    
    /**
     * Validate string length
     * @param value String to validate
     * @param maxLength Maximum allowed length
     * @return true if valid
     */
    public static boolean isValidLength(String value, int maxLength) {
        return value != null && value.length() <= maxLength;
    }
    
    /**
     * Validate string length range
     * @param value String to validate
     * @param minLength Minimum length
     * @param maxLength Maximum length
     * @return true if valid
     */
    public static boolean isValidLengthRange(String value, int minLength, int maxLength) {
        return value != null && value.length() >= minLength && value.length() <= maxLength;
    }
    
    // ==================== Email Validation ====================
    
    /**
     * Validate email format
     * @param email Email to validate
     * @return true if valid email format
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim().toLowerCase()).matches();
    }
    
    // ==================== Phone Validation ====================
    
    /**
     * Validate phone format
     * @param phone Phone to validate
     * @return true if valid phone format
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }
    
    // ==================== Academic Validation ====================
    
    /**
     * Validate course code format
     * @param courseCode Course code to validate
     * @return true if valid format
     */
    public static boolean isValidCourseCode(String courseCode) {
        if (courseCode == null || courseCode.trim().isEmpty()) {
            return false;
        }
        return COURSE_CODE_PATTERN.matcher(courseCode.trim().toUpperCase()).matches();
    }
    
    /**
     * Validate roll number format
     * @param rollNo Roll number to validate
     * @return true if valid format
     */
    public static boolean isValidRollNo(String rollNo) {
        if (rollNo == null || rollNo.trim().isEmpty()) {
            return false;
        }
        return ROLL_NO_PATTERN.matcher(rollNo.trim().toUpperCase()).matches();
    }
    
    /**
     * Validate employee ID format
     * @param employeeId Employee ID to validate
     * @return true if valid format
     */
    public static boolean isValidEmployeeId(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            return false;
        }
        return EMPLOYEE_ID_PATTERN.matcher(employeeId.trim().toUpperCase()).matches();
    }
    
    /**
     * Validate year value
     * @param year Year to validate
     * @param minYear Minimum valid year
     * @param maxYear Maximum valid year
     * @return true if valid
     */
    public static boolean isValidYear(int year, int minYear, int maxYear) {
        return year >= minYear && year <= maxYear;
    }
    
    /**
     * Validate academic year (1-10)
     * @param year Year to validate
     * @return true if valid
     */
    public static boolean isValidAcademicYear(int year) {
        return year >= 1 && year <= 10;
    }
    
    /**
     * Validate credits
     * @param credits Credits to validate
     * @param minCredits Minimum credits
     * @param maxCredits Maximum credits
     * @return true if valid
     */
    public static boolean isValidCredits(int credits, int minCredits, int maxCredits) {
        return credits >= minCredits && credits <= maxCredits;
    }
    
    /**
     * Validate semester name
     * @param semester Semester to validate
     * @return true if valid semester name
     */
    public static boolean isValidSemester(String semester) {
        if (semester == null || semester.trim().isEmpty()) {
            return false;
        }
        String sem = semester.trim().toLowerCase();
        return sem.equals("Monsoon") || 
               sem.equals("summer") || sem.equals("winter");
    }
    
    // ==================== Numeric Validation ====================
    
    /**
     * Validate positive integer
     * @param value Value to validate
     * @return true if positive
     */
    public static boolean isPositive(int value) {
        return value > 0;
    }
    
    /**
     * Validate non-negative integer
     * @param value Value to validate
     * @return true if non-negative
     */
    public static boolean isNonNegative(int value) {
        return value >= 0;
    }
    
    /**
     * Validate integer range
     * @param value Value to validate
     * @param min Minimum value (inclusive)
     * @param max Maximum value (inclusive)
     * @return true if in range
     */
    public static boolean isInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }
    
    /**
     * Validate double range
     * @param value Value to validate
     * @param min Minimum value (inclusive)
     * @param max Maximum value (inclusive)
     * @return true if in range
     */
    public static boolean isInRange(double value, double min, double max) {
        return value >= min && value <= max;
    }
    
    /**
     * Validate percentage (0-100)
     * @param value Value to validate
     * @return true if valid percentage
     */
    public static boolean isValidPercentage(double value) {
        return value >= 0.0 && value <= 100.0;
    }
    
    // ==================== Date/Time Validation ====================
    
    /**
     * Check if date is in the past
     * @param dateTime DateTime to check
     * @return true if in the past
     */
    public static boolean isInPast(LocalDateTime dateTime) {
        return dateTime != null && dateTime.isBefore(LocalDateTime.now());
    }
    
    /**
     * Check if date is in the future
     * @param dateTime DateTime to check
     * @return true if in the future
     */
    public static boolean isInFuture(LocalDateTime dateTime) {
        return dateTime != null && dateTime.isAfter(LocalDateTime.now());
    }
    
    /**
     * Check if date is today or in the future
     * @param dateTime DateTime to check
     * @return true if today or future
     */
    public static boolean isTodayOrFuture(LocalDateTime dateTime) {
        return dateTime != null && !dateTime.isBefore(LocalDateTime.now().toLocalDate().atStartOfDay());
    }
    
    // ==================== Grade Validation ====================
    
    /**
     * Validate letter grade
     * @param grade Grade to validate
     * @return true if valid letter grade
     */
    public static boolean isValidLetterGrade(String grade) {
        if (grade == null || grade.trim().isEmpty()) {
            return false;
        }
        String[] validGrades = {
            "A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", 
            "D+", "D", "F", "W", "I", "P", "NP"
        };
        String upperGrade = grade.trim().toUpperCase();
        for (String valid : validGrades) {
            if (valid.equals(upperGrade)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Validate score against max score
     * @param score Score achieved
     * @param maxScore Maximum possible score
     * @return true if valid
     */
    public static boolean isValidScore(double score, double maxScore) {
        return score >= 0 && score <= maxScore;
    }
    
    // ==================== Capacity Validation ====================
    
    /**
     * Validate section capacity
     * @param capacity Capacity to validate
     * @param minCapacity Minimum capacity
     * @param maxCapacity Maximum capacity
     * @return true if valid
     */
    public static boolean isValidCapacity(int capacity, int minCapacity, int maxCapacity) {
        return capacity >= minCapacity && capacity <= maxCapacity;
    }
    
    /**
     * Check if enrollment count is valid for capacity
     * @param enrolledCount Enrolled count
     * @param capacity Total capacity
     * @return true if valid
     */
    public static boolean isValidEnrollmentCount(int enrolledCount, int capacity) {
        return enrolledCount >= 0 && enrolledCount <= capacity;
    }
    
    // ==================== Collection Validation ====================
    
    /**
     * Check if list is not null and not empty
     * @param list List to check
     * @return true if not null and not empty
     */
    public static boolean isNotEmpty(List<?> list) {
        return list != null && !list.isEmpty();
    }
    
    /**
     * Check if list has minimum size
     * @param list List to check
     * @param minSize Minimum size
     * @return true if meets minimum size
     */
    public static boolean hasMinimumSize(List<?> list, int minSize) {
        return list != null && list.size() >= minSize;
    }
    
    // ==================== Helper Methods ====================
    
    /**
     * Sanitize string input (trim and handle null)
     * @param value String to sanitize
     * @return Sanitized string or null
     */
    public static String sanitize(String value) {
        return value != null ? value.trim() : null;
    }
    
    /**
     * Sanitize and uppercase string
     * @param value String to sanitize
     * @return Uppercase sanitized string or null
     */
    public static String sanitizeAndUppercase(String value) {
        return value != null ? value.trim().toUpperCase() : null;
    }
    
    /**
     * Sanitize and lowercase string
     * @param value String to sanitize
     * @return Lowercase sanitized string or null
     */
    public static String sanitizeAndLowercase(String value) {
        return value != null ? value.trim().toLowerCase() : null;
    }
    
    /**
     * Validate multiple conditions with AND logic
     * @param conditions Boolean conditions to check
     * @return true if all conditions are true
     */
    public static boolean validateAll(boolean... conditions) {
        for (boolean condition : conditions) {
            if (!condition) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Validate at least one condition is true (OR logic)
     * @param conditions Boolean conditions to check
     * @return true if any condition is true
     */
    public static boolean validateAny(boolean... conditions) {
        for (boolean condition : conditions) {
            if (condition) {
                return true;
            }
        }
        return false;
    }
}