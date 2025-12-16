package edu.univ.erp.domain;

import java.util.Objects;

/**
 * Grade Domain Model
 * MODIFIED: Changed to store user_id, section_id, quiz, midsem, endsem
 * Simplified structure for direct grade storage per student per section
 */
public class Grade {
    
    // Fields
    private int gradeId;
    private int userId;          // Student's user_id
    private int sectionId;       // Section ID
    private Double quiz;         // Quiz score (out of 20)
    private Double midsem;       // Midsem score (out of 30)
    private Double endsem;       // Endsem score (out of 50)
    private String studentName;
    private String studentRollNo;
    private String courseCode;
    private String courseTitle;
    private String sectionName;
    private String semester;
    private int year;
    private int credits;
    
    // Max scores (constants)
    public static final double QUIZ_MAX = 20.0;
    public static final double MIDSEM_MAX = 30.0;
    public static final double ENDSEM_MAX = 50.0;
    public static final double TOTAL_MAX = 100.0;
    
    // Weights (constants)
    public static final double QUIZ_WEIGHT = 20.0;
    public static final double MIDSEM_WEIGHT = 30.0;
    public static final double ENDSEM_WEIGHT = 50.0;
    
    // Constructors
    public Grade() {
    }
    
    public Grade(int userId, int sectionId) {
        this.userId = userId;
        this.sectionId = sectionId;
    }
    
    public Grade(int userId, int sectionId, Double quiz, Double midsem, Double endsem) {
        this.userId = userId;
        this.sectionId = sectionId;
        this.quiz = quiz;
        this.midsem = midsem;
        this.endsem = endsem;
    }
    
    // Getters and Setters

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentRollNo() {
        return studentRollNo;
    }

    public void setStudentRollNo(String studentRollNo) {
        this.studentRollNo = studentRollNo;
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
    
    public String getSectionName() {
        return sectionName;
    }
    
    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
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
    
    public int getCredits() {
        return credits;
    }
    
    public void setCredits(int credits) {
        this.credits = credits;
    }

    public int getGradeId() {
        return gradeId;
    }
    
    public void setGradeId(int gradeId) {
        this.gradeId = gradeId;
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
    
    public int getSectionId() {
        return sectionId;
    }
    
    public void setSectionId(int sectionId) {
        if (sectionId <= 0) {
            throw new IllegalArgumentException("Section ID must be positive");
        }
        this.sectionId = sectionId;
    }
    
    public Double getQuiz() {
        return quiz;
    }
    
    public void setQuiz(Double quiz) {
        if (quiz != null) {
            if (quiz < 0 || quiz > QUIZ_MAX) {
                throw new IllegalArgumentException("Quiz score must be between 0 and " + QUIZ_MAX);
            }
        }
        this.quiz = quiz;
    }
    
    public Double getMidsem() {
        return midsem;
    }
    
    public void setMidsem(Double midsem) {
        if (midsem != null) {
            if (midsem < 0 || midsem > MIDSEM_MAX) {
                throw new IllegalArgumentException("Midsem score must be between 0 and " + MIDSEM_MAX);
            }
        }
        this.midsem = midsem;
    }
    
    public Double getEndsem() {
        return endsem;
    }
    
    public void setEndsem(Double endsem) {
        if (endsem != null) {
            if (endsem < 0 || endsem > ENDSEM_MAX) {
                throw new IllegalArgumentException("Endsem score must be between 0 and " + ENDSEM_MAX);
            }
        }
        this.endsem = endsem;
    }
    
    // Business Logic Methods
    
    /**
     * Check if quiz score has been entered
     */
    public boolean hasQuiz() {
        return quiz != null;
    }
    
    /**
     * Check if midsem score has been entered
     */
    public boolean hasMidsem() {
        return midsem != null;
    }
    
    /**
     * Check if endsem score has been entered
     */
    public boolean hasEndsem() {
        return endsem != null;
    }
    
    /**
     * Check if all grades have been entered
     */
    public boolean hasAllGrades() {
        return hasQuiz() && hasMidsem() && hasEndsem();
    }
    
    /**
     * Get total score (sum of all components)
     */
    public double getTotalScore() {
        double total = 0.0;
        if (quiz != null) total += quiz;
        if (midsem != null) total += midsem;
        if (endsem != null) total += endsem;
        return total;
    }
    
    /**
     * Get percentage score (0-100)
     */
    public double getPercentage() {
        if (!hasAllGrades()) return 0.0;
        return getTotalScore();
    }
    
    /**
     * Get letter grade based on total score
     */
    public String getLetterGrade() {
        if (!hasAllGrades()) return "N/A";
        
        double percentage = getPercentage();
        
        if (percentage >= 90) return "A+";
        if (percentage >= 85) return "A";
        if (percentage >= 80) return "A-";
        if (percentage >= 75) return "B+";
        if (percentage >= 70) return "B";
        if (percentage >= 65) return "B-";
        if (percentage >= 60) return "C+";
        if (percentage >= 55) return "C";
        if (percentage >= 50) return "C-";
        if (percentage >= 45) return "D";
        return "F";
    }
    
    /**
     * Check if passing (>= 50%)
     */
    public boolean isPassing() {
        return hasAllGrades() && getPercentage() >= 50.0;
    }
    
    /**
     * Get quiz score display
     */
    public String getQuizDisplay() {
        if (hasQuiz()) {
            return String.format("%.0f/%.0f", quiz, QUIZ_MAX);
        }
        return "N/A";
    }
    
    /**
     * Get midsem score display
     */
    public String getMidsemDisplay() {
        if (hasMidsem()) {
            return String.format("%.0f/%.0f", midsem, MIDSEM_MAX);
        }
        return "N/A";
    }
    
    /**
     * Get endsem score display
     */
    public String getEndsemDisplay() {
        if (hasEndsem()) {
            return String.format("%.0f/%.0f", endsem, ENDSEM_MAX);
        }
        return "N/A";
    }
    
    /**
     * Get total score display
     */
    public String getTotalDisplay() {
        if (hasAllGrades()) {
            return String.format("%.2f/%.0f", getTotalScore(), TOTAL_MAX);
        }
        return "N/A";
    }
    
    /**
     * Get percentage display
     */
    public String getPercentageDisplay() {
        if (hasAllGrades()) {
            return String.format("%.2f%%", getPercentage());
        }
        return "N/A";
    }
    
    /**
     * Get performance level description
     */
    public String getPerformanceLevel() {
        if (!hasAllGrades()) return "Not Graded";
        double pct = getPercentage();
        if (pct >= 90) return "Excellent";
        if (pct >= 80) return "Very Good";
        if (pct >= 70) return "Good";
        if (pct >= 60) return "Satisfactory";
        if (pct >= 50) return "Passing";
        return "Needs Improvement";
    }
    
    /**
     * Validate all required fields are present
     */
    public boolean isValid() {
        return userId > 0 && sectionId > 0;
    }
    
    // Utility Methods
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Grade grade = (Grade) o;
        return gradeId == grade.gradeId &&
               userId == grade.userId &&
               sectionId == grade.sectionId;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(gradeId, userId, sectionId);
    }
    
    @Override
    public String toString() {
        return "Grade{" +
                "gradeId=" + gradeId +
                ", userId=" + userId +
                ", sectionId=" + sectionId +
                ", quiz=" + getQuizDisplay() +
                ", midsem=" + getMidsemDisplay() +
                ", endsem=" + getEndsemDisplay() +
                ", total=" + getTotalDisplay() +
                ", letterGrade=" + getLetterGrade() +
                '}';
    }
    
    /**
     * Create a copy of this grade
     */
    public Grade copy() {
        Grade copy = new Grade();
        copy.gradeId = this.gradeId;
        copy.userId = this.userId;
        copy.sectionId = this.sectionId;
        copy.quiz = this.quiz;
        copy.midsem = this.midsem;
        copy.endsem = this.endsem;
        return copy;
    }
}
