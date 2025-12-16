package edu.univ.erp.domain;

import java.util.Objects;
import java.time.LocalDateTime;

/**
 * Section Domain Model
 * Represents a course section in the ERP Database
 * Links a course to an instructor with schedule and capacity information
 * Added status field (OPEN/CLOSED) and drop_deadline field (DATETIME)
 * FIXED: Changed dropDeadline from LocalDate to LocalDateTime
 * IMPROVED: Added semester constants for system-wide consistency
 */
public class Section {
    
    // Fields
    private int sectionId;
    private int courseId;
    private Integer instructorId; // Can be null if not yet assigned
    private String sectionName;
    private String dayTime;
    private String room;
    private int capacity;
    private int enrolledCount;
    private String semester;
    private int year;
    private String status; // OPEN or CLOSED
    private LocalDateTime dropDeadline; // FIXED: Changed from LocalDate to LocalDateTime
    
    // Additional fields from JOINs (for display purposes)
    private String courseCode;
    private String courseTitle;
    private int credits;
    private String description;
    private String instructorName;
    
    // Constants - IMPROVED: Semester system constants
    public static final int MIN_CAPACITY = 1;
    public static final int MAX_CAPACITY = 500;
    
    // IMPROVED: Semester constants for consistent naming across the system
    public static final String SEMESTER_MONSOON = "Monsoon";
    public static final String SEMESTER_WINTER = "Winter";
    public static final String SEMESTER_SUMMER = "Summer";
    
    public static final String STATUS_OPEN = "OPEN";
    public static final String STATUS_CLOSED = "CLOSED";
    
    // Constructors
    public Section() {
        this.enrolledCount = 0;
        this.status = STATUS_OPEN; // Default to open
    }
    
    public Section(int courseId, String sectionName, String semester, int year, int capacity) {
        this();
        this.courseId = courseId;
        this.sectionName = sectionName;
        this.semester = semester;
        this.year = year;
        this.capacity = capacity;
    }
    
    // Getters and Setters
    public int getSectionId() {
        return sectionId;
    }
    
    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }
    
    public int getCourseId() {
        return courseId;
    }
    
    public void setCourseId(int courseId) {
        if (courseId <= 0) {
            throw new IllegalArgumentException("Course ID must be positive");
        }
        this.courseId = courseId;
    }
    
    public Integer getInstructorId() {
        return instructorId;
    }
    
    public void setInstructorId(Integer instructorId) {
        if (instructorId != null && instructorId <= 0) {
            throw new IllegalArgumentException("Instructor ID must be positive if provided");
        }
        this.instructorId = instructorId;
    }
    
    public String getSectionName() {
        return sectionName;
    }
    
    public void setSectionName(String sectionName) {
        if (sectionName == null || sectionName.trim().isEmpty()) {
            throw new IllegalArgumentException("Section name cannot be empty");
        }
        if (sectionName.length() > 10) {
            throw new IllegalArgumentException("Section name cannot exceed 10 characters");
        }
        this.sectionName = sectionName.trim().toUpperCase();
    }
    
    public String getDayTime() {
        return dayTime;
    }
    
    public void setDayTime(String dayTime) {
        if (dayTime == null || dayTime.trim().isEmpty()) {
            throw new IllegalArgumentException("Day/Time cannot be empty");
        }
        if (dayTime.length() > 100) {
            throw new IllegalArgumentException("Day/Time cannot exceed 100 characters");
        }
        this.dayTime = dayTime.trim();
    }
    
    public String getRoom() {
        return room;
    }
    
    public void setRoom(String room) {
        if (room != null && !room.trim().isEmpty()) {
            if (room.length() > 50) {
                throw new IllegalArgumentException("Room cannot exceed 50 characters");
            }
            this.room = room.trim();
        } else {
            this.room = room;
        }
    }
    
    public int getCapacity() {
        return capacity;
    }
    
    public void setCapacity(int capacity) {
        if (capacity < MIN_CAPACITY || capacity > MAX_CAPACITY) {
            throw new IllegalArgumentException(
                "Capacity must be between " + MIN_CAPACITY + " and " + MAX_CAPACITY);
        }
        this.capacity = capacity;
    }
    
    public int getEnrolledCount() {
        return enrolledCount;
    }
    
    public void setEnrolledCount(int enrolledCount) {
        if (enrolledCount < 0) {
            throw new IllegalArgumentException("Enrolled count cannot be negative");
        }
        if (enrolledCount > capacity) {
            throw new IllegalArgumentException("Enrolled count cannot exceed capacity");
        }
        this.enrolledCount = enrolledCount;
    }
    
    public String getSemester() {
        return semester;
    }
    
    public void setSemester(String semester) {
        if (semester == null || semester.trim().isEmpty()) {
            throw new IllegalArgumentException("Semester cannot be empty");
        }
        if (semester.length() > 20) {
            throw new IllegalArgumentException("Semester cannot exceed 20 characters");
        }
        this.semester = semester.trim();
    }
    
    public int getYear() {
        return year;
    }
    
    public void setYear(int year) {
        if (year < 2000 || year > 2100) {
            throw new IllegalArgumentException("Year must be between 2000 and 2100");
        }
        this.year = year;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        if (status != null) {
            String upperStatus = status.trim().toUpperCase();
            if (!STATUS_OPEN.equals(upperStatus) && !STATUS_CLOSED.equals(upperStatus)) {
                throw new IllegalArgumentException("Status must be OPEN or CLOSED");
            }
            this.status = upperStatus;
        } else {
            this.status = STATUS_OPEN; // Default to open
        }
    }
    
    // FIXED: Drop deadline getter and setter - now uses LocalDateTime
    public LocalDateTime getDropDeadline() {
        return dropDeadline;
    }
    
    public void setDropDeadline(LocalDateTime dropDeadline) {
        this.dropDeadline = dropDeadline;
    }
    
    // Additional fields from JOINs
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getInstructorName() {
        return instructorName;
    }
    
    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
    }
    
    // Business Logic Methods
    
    /**
     * Check if section has available seats
     * @return true if enrolled count is less than capacity
     */
    public boolean hasSeats() {
        return enrolledCount < capacity;
    }
    
    /**
     * Check if section is available for registration (has seats AND is open)
     * @return true if section can accept registrations
     */
    public boolean isAvailableForRegistration() {
        return hasSeats() && STATUS_OPEN.equals(status);
    }
    
    /**
     * Get number of available seats
     * @return Available seats count
     */
    public int getAvailableSeats() {
        return Math.max(0, capacity - enrolledCount);
    }
    
    /**
     * Check if section is full
     * @return true if no seats available
     */
    public boolean isFull() {
        return enrolledCount >= capacity;
    }
    
    /**
     * Check if section is closed
     * @return true if status is CLOSED
     */
    public boolean isClosed() {
        return STATUS_CLOSED.equals(status);
    }
    
    /**
     * Check if section is open
     * @return true if status is OPEN
     */
    public boolean isOpen() {
        return STATUS_OPEN.equals(status);
    }
    
    /**
     * FIXED: Check if drop is allowed (before deadline) - now uses LocalDateTime
     * @return true if now is before or equal to drop deadline
     */
    public boolean canDrop() {
        if (dropDeadline == null) {
            return true; // No deadline set, allow drop
        }
        return !LocalDateTime.now().isAfter(dropDeadline);
    }
    
    /**
     * Get enrollment percentage
     * @return Percentage of capacity filled
     */
    public double getEnrollmentPercentage() {
        if (capacity == 0) return 0.0;
        return (enrolledCount * 100.0) / capacity;
    }
    
    /**
     * Check if section has instructor assigned
     * @return true if instructor is assigned
     */
    public boolean hasInstructor() {
        return instructorId != null && instructorId > 0;
    }
    
    /**
     * Check if section has room assigned
     * @return true if room is set
     */
    public boolean hasRoom() {
        return room != null && !room.isEmpty();
    }
    
    /**
     * Increment enrolled count (for enrollment)
     * @return true if successful
     */
    public boolean incrementEnrollment() {
        if (hasSeats()) {
            enrolledCount++;
            return true;
        }
        return false;
    }
    
    /**
     * Decrement enrolled count (for drop)
     * @return true if successful
     */
    public boolean decrementEnrollment() {
        if (enrolledCount > 0) {
            enrolledCount--;
            return true;
        }
        return false;
    }
    
    /**
     * Get display name for section
     * @return Course code, section name, and semester/year
     */
    public String getDisplayName() {
        if (courseCode != null) {
            return courseCode + " " + sectionName + " (" + semester + " " + year + ")";
        }
        return "Section " + sectionName + " (" + semester + " " + year + ")";
    }
    
    /**
     * Get full display with schedule
     * @return Complete section information
     */
    public String getFullDisplayName() {
        StringBuilder sb = new StringBuilder(getDisplayName());
        if (dayTime != null) {
            sb.append(" - ").append(dayTime);
        }
        if (room != null) {
            sb.append(" [").append(room).append("]");
        }
        return sb.toString();
    }
    
    /**
     * Get enrollment status description
     * @return Status string (e.g., "25/30 seats")
     */
    public String getEnrollmentStatus() {
        return enrolledCount + "/" + capacity + " seats";
    }
    
    /**
     * Validate all required fields are present
     * @return true if all required fields are valid
     */
    public boolean isValid() {
        return courseId > 0 &&
               sectionName != null && !sectionName.isEmpty() &&
               dayTime != null && !dayTime.isEmpty() &&
               capacity >= MIN_CAPACITY && capacity <= MAX_CAPACITY &&
               semester != null && !semester.isEmpty() &&
               year >= 2000 && year <= 2100;
    }
    
    /**
     * Check if section matches semester and year
     * @param sem Semester to check
     * @param yr Year to check
     * @return true if matches
     */
    public boolean isInTerm(String sem, int yr) {
        return semester.equalsIgnoreCase(sem) && year == yr;
    }
    
    /**
     * Check if section is nearly full (>= 90% capacity)
     * @return true if nearly full
     */
    public boolean isNearlyFull() {
        return getEnrollmentPercentage() >= 90.0;
    }
    
    /**
     * Check if section is low enrollment (< 25% capacity)
     * @return true if low enrollment
     */
    public boolean isLowEnrollment() {
        return getEnrollmentPercentage() < 25.0;
    }
    
    // Utility Methods
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Section section = (Section) o;
        return sectionId == section.sectionId &&
               courseId == section.courseId &&
               Objects.equals(sectionName, section.sectionName) &&
               Objects.equals(semester, section.semester) &&
               year == section.year;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(sectionId, courseId, sectionName, semester, year);
    }
    
    @Override
    public String toString() {
        return "Section{" +
                "sectionId=" + sectionId +
                ", courseId=" + courseId +
                ", courseCode='" + courseCode + '\'' +
                ", sectionName='" + sectionName + '\'' +
                ", semester='" + semester + '\'' +
                ", year=" + year +
                ", enrollment=" + enrolledCount + "/" + capacity +
                ", status='" + status + '\'' +
                ", dayTime='" + dayTime + '\'' +
                ", room='" + room + '\'' +
                ", instructorId=" + instructorId +
                ", dropDeadline=" + dropDeadline +
                '}';
    }
    
    /**
     * Create a copy of this section
     * @return New Section object with same values
     */
    public Section copy() {
        Section copy = new Section();
        copy.sectionId = this.sectionId;
        copy.courseId = this.courseId;
        copy.instructorId = this.instructorId;
        copy.sectionName = this.sectionName;
        copy.dayTime = this.dayTime;
        copy.room = this.room;
        copy.capacity = this.capacity;
        copy.enrolledCount = this.enrolledCount;
        copy.semester = this.semester;
        copy.year = this.year;
        copy.status = this.status;
        copy.dropDeadline = this.dropDeadline; // FIXED: Now LocalDateTime
        copy.courseCode = this.courseCode;
        copy.courseTitle = this.courseTitle;
        copy.credits = this.credits;
        copy.description = this.description;
        copy.instructorName = this.instructorName;
        return copy;
    }
}
