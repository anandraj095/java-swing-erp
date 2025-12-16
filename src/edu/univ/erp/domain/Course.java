package edu.univ.erp.domain;

import java.util.Objects;

/**
 * Course Domain Model
 * Represents a course in the ERP Database
 * Contains course information including code, title, credits, and prerequisites
 */
public class Course {
    
    // Fields
    private int courseId;
    private String courseCode;
    private String title;
    private int credits;
    private String description;
    private String prerequisite;
    
    // Constants
    public static final int MIN_CREDITS = 1;
    public static final int MAX_CREDITS = 6;
    
    // Constructors
    public Course() {
    }
    
    public Course(String courseCode, String title, int credits) {
        this.courseCode = courseCode;
        this.title = title;
        this.credits = credits;
    }
    
    // Getters and Setters
    public int getCourseId() {
        return courseId;
    }
    
    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }
    
    public String getCourseCode() {
        return courseCode;
    }
    
    public void setCourseCode(String courseCode) {
        if (courseCode == null || courseCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Course code cannot be empty");
        }
        if (courseCode.length() > 20) {
            throw new IllegalArgumentException("Course code cannot exceed 20 characters");
        }
        this.courseCode = courseCode.trim().toUpperCase();
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Course title cannot be empty");
        }
        if (title.length() > 200) {
            throw new IllegalArgumentException("Course title cannot exceed 200 characters");
        }
        this.title = title.trim();
    }
    
    public int getCredits() {
        return credits;
    }
    
    public void setCredits(int credits) {
        if (credits < MIN_CREDITS || credits > MAX_CREDITS) {
            throw new IllegalArgumentException(
                "Credits must be between " + MIN_CREDITS + " and " + MAX_CREDITS);
        }
        this.credits = credits;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        if (description != null && !description.trim().isEmpty()) {
            this.description = description.trim();
        } else {
            this.description = description;
        }
    }
    
    public String getPrerequisite() {
        return prerequisite;
    }
    
    public void setPrerequisite(String prerequisite) {
        if (prerequisite != null && !prerequisite.trim().isEmpty()) {
            if (prerequisite.length() > 100) {
                throw new IllegalArgumentException("Prerequisite cannot exceed 100 characters");
            }
            this.prerequisite = prerequisite.trim();
        } else {
            this.prerequisite = prerequisite;
        }
    }
    
    // Business Logic Methods
    
    /**
     * Check if course has prerequisites
     * @return true if prerequisite is set
     */
    public boolean hasPrerequisite() {
        return prerequisite != null && !prerequisite.isEmpty();
    }
    
    /**
     * Check if course has description
     * @return true if description is set
     */
    public boolean hasDescription() {
        return description != null && !description.isEmpty();
    }
    
    /**
     * Get display name for course
     * @return Course code and title
     */
    public String getDisplayName() {
        return courseCode + " - " + title;
    }
    
    /**
     * Get full display with credits
     * @return Course code, title, and credits
     */
    public String getFullDisplayName() {
        return courseCode + " - " + title + " (" + credits + " credits)";
    }
    
    /**
     * Validate all required fields are present
     * @return true if all required fields are valid
     */
    public boolean isValid() {
        return courseCode != null && !courseCode.isEmpty() &&
               title != null && !title.isEmpty() &&
               credits >= MIN_CREDITS && credits <= MAX_CREDITS;
    }
    
    /**
     * Check if course matches a search query
     * @param query Search query
     * @return true if course code or title contains query (case-insensitive)
     */
    public boolean matchesSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }
        String lowerQuery = query.toLowerCase();
        return (courseCode != null && courseCode.toLowerCase().contains(lowerQuery)) ||
               (title != null && title.toLowerCase().contains(lowerQuery)) ||
               (description != null && description.toLowerCase().contains(lowerQuery));
    }
    
    /**
     * Get credit level category
     * @return Credit level description
     */
    public String getCreditLevel() {
        return switch (credits) {
            case 1 -> "Light";
            case 2 -> "Standard";
            case 3 -> "Regular";
            case 4 -> "Heavy";
            default -> "Intensive";
        };
    }
    
    /**
     * Check if this is a high-credit course
     * @return true if credits >= 4
     */
    public boolean isHighCreditCourse() {
        return credits >= 4;
    }
    
    // Utility Methods
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return courseId == course.courseId && 
               Objects.equals(courseCode, course.courseCode);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(courseId, courseCode);
    }
    
    @Override
    public String toString() {
        return "Course{" +
                "courseId=" + courseId +
                ", courseCode='" + courseCode + '\'' +
                ", title='" + title + '\'' +
                ", credits=" + credits +
                ", hasPrerequisite=" + hasPrerequisite() +
                '}';
    }
    
    /**
     * Get detailed string representation
     * @return Detailed course information
     */
    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Course: ").append(courseCode).append("\n");
        sb.append("Title: ").append(title).append("\n");
        sb.append("Credits: ").append(credits).append("\n");
        if (hasDescription()) {
            sb.append("Description: ").append(description).append("\n");
        }
        if (hasPrerequisite()) {
            sb.append("Prerequisites: ").append(prerequisite).append("\n");
        }
        return sb.toString();
    }
    
    /**
     * Create a copy of this course
     * @return New Course object with same values
     */
    public Course copy() {
        Course copy = new Course();
        copy.courseId = this.courseId;
        copy.courseCode = this.courseCode;
        copy.title = this.title;
        copy.credits = this.credits;
        copy.description = this.description;
        copy.prerequisite = this.prerequisite;
        return copy;
    }
}