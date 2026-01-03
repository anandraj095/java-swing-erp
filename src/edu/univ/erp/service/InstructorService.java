package edu.univ.erp.service;

import edu.univ.erp.data.ERPDatabase;
import edu.univ.erp.domain.*;

import java.sql.SQLException;
import java.util.*;

/**
 * InstructorService - Handles all instructor-related operations
 * MODIFIED: Updated to work with new grades table format
 */
public class InstructorService {
    
    private final ERPDatabase erpDb;
    private final AccessControlService accessControl;
    private final GradeComputationService gradeComputation;
    
    public InstructorService(ERPDatabase erpDb, AccessControlService accessControl,
                            GradeComputationService gradeComputation) {
        this.erpDb = erpDb;
        this.accessControl = accessControl;
        this.gradeComputation = gradeComputation;
    }
    
    /**
     * Gets all sections taught by an instructor for a semester
     */
    public List<Section> getMySections(int instructorId, String semester, int year) throws SQLException {
        return erpDb.getSectionsByInstructor(instructorId, semester, year);
    }
    
    /**
     * Checks if instructor teaches a specific section
     */
    public boolean isMySection(int instructorId, int sectionId) throws SQLException {
        var sectionOpt = erpDb.getSectionById(sectionId);
        if (sectionOpt.isEmpty()) {
            return false;
        }
        
        Section section = sectionOpt.get();
        return section.getInstructorId() != null && 
               section.getInstructorId() == instructorId;
    }
    
    /**
     * Gets all students enrolled in a section
     */
    public ServiceResult<List<Enrollment>> getSectionRoster(int instructorId, int sectionId) {
        try {
            if (!isMySection(instructorId, sectionId)) {
                return ServiceResult.error("Access denied: This is not your section");
            }
            
            List<Enrollment> enrollments = erpDb.getSectionEnrollments(sectionId);
            return ServiceResult.success("Roster retrieved", enrollments);
            
        } catch (SQLException e) {
            return ServiceResult.error("Database error: " + e.getMessage());
        }
    }
    
    /**
     * MODIFIED: Enter grade for a student using new format
     */
    public ServiceResult enterGrade(int instructorId, int userId, int sectionId,
                                   Double quiz, Double midsem, Double endsem) {
        try {
            // Check maintenance mode
            String validationError = accessControl.validateOperation("INSTRUCTOR", true);
            if (validationError != null) {
                return ServiceResult.error(validationError);
            }
            
            // Verify instructor teaches this section
            if (!isMySection(instructorId, sectionId)) {
                return ServiceResult.error("Access denied: This is not your section");
            }
            
            // Validate scores
            if (quiz != null && (quiz < 0 || quiz > Grade.QUIZ_MAX)) {
                return ServiceResult.error("Quiz score must be between 0 and " + Grade.QUIZ_MAX);
            }
            if (midsem != null && (midsem < 0 || midsem > Grade.MIDSEM_MAX)) {
                return ServiceResult.error("Midsem score must be between 0 and " + Grade.MIDSEM_MAX);
            }
            if (endsem != null && (endsem < 0 || endsem > Grade.ENDSEM_MAX)) {
                return ServiceResult.error("Endsem score must be between 0 and " + Grade.ENDSEM_MAX);
            }
            
            // Save or update grade
            erpDb.saveOrUpdateGrade(userId, sectionId, quiz, midsem, endsem);
            
            return ServiceResult.success("Grades saved successfully");
            
        } catch (SQLException e) {
            return ServiceResult.error("Database error: " + e.getMessage());
        }
    }
    
    /**
     * MODIFIED: Compute final grade for a student
     */
    public ServiceResult computeFinalGrade(int instructorId, int userId, int sectionId) {
        try {
            String validationError = accessControl.validateOperation("INSTRUCTOR", true);
            if (validationError != null) {
                return ServiceResult.error(validationError);
            }
            
            if (!isMySection(instructorId, sectionId)) {
                return ServiceResult.error("Access denied: This is not your section");
            }
            
            // Get grade
            var gradeOpt = erpDb.getGradeByUserAndSection(userId, sectionId);
            if (gradeOpt.isEmpty()) {
                return ServiceResult.error("No grades entered yet");
            }
            
            Grade grade = gradeOpt.get();
            if (!grade.hasAllGrades()) {
                return ServiceResult.error("All grade components (quiz, midsem, endsem) must be entered");
            }
            
            // Calculate final grade
            String letterGrade = grade.getLetterGrade();
            double percentage = grade.getPercentage();
            
            // Update final grade in enrollment
            var studentOpt = erpDb.getStudentByUserId(userId);
            if (studentOpt.isEmpty()) {
                return ServiceResult.error("Student not found");
            }
            
            int studentId = studentOpt.get().getStudentId();
            var enrollments = erpDb.getStudentEnrollments(studentId, null);
            for (Enrollment enrollment : enrollments) {
                if (enrollment.getSectionId() == sectionId) {
                    erpDb.updateFinalGrade(enrollment.getEnrollmentId(), letterGrade);
                    break;
                }
            }
            
            return ServiceResult.success("Final grade computed: " + letterGrade + 
                                       " (" + String.format("%.2f", percentage) + "%)");
            
        } catch (SQLException e) {
            return ServiceResult.error("Database error: " + e.getMessage());
        }
    }
    
    /**
     * MODIFIED: Compute final grades for all students in a section
     */
    public ServiceResult<Map<Integer, String>> computeAllFinalGrades(int instructorId, int sectionId) {
        try {
            if (!isMySection(instructorId, sectionId)) {
                return ServiceResult.error("Access denied: This is not your section");
            }
            
            List<Enrollment> enrollments = erpDb.getSectionEnrollmentsAll(sectionId);
            Map<Integer, String> results = new HashMap<>();
            
            for (Enrollment enrollment : enrollments) {
                // Get student's user_id
                var studentOpt = erpDb.getStudentById(enrollment.getStudentId());
                if (studentOpt.isEmpty()) {
                    results.put(enrollment.getStudentId(), "Student not found");
                    continue;
                }
                
                int userId = studentOpt.get().getUserId();
                ServiceResult result = computeFinalGrade(instructorId, userId, sectionId);
                results.put(enrollment.getStudentId(), result.getMessage());
            }
            
            return ServiceResult.success("Final grades computed for all students", results);
            
        } catch (SQLException e) {
            return ServiceResult.error("Database error: " + e.getMessage());
        }
    }
    
    /**
     * MODIFIED: Get class statistics for a section
     */
    public ServiceResult<ClassStatistics> getClassStatistics(int instructorId, int sectionId) {
        try {
            if (!isMySection(instructorId, sectionId)) {
                return ServiceResult.error("Access denied: This is not your section");
            }
            
            List<Enrollment> enrollments = erpDb.getSectionEnrollmentsAll(sectionId);
            List<Grade> grades = erpDb.getGradesBySection(sectionId);
            
            ClassStatistics stats = gradeComputation.computeStatisticsNew(enrollments, grades, erpDb);
            
            return ServiceResult.success("Statistics computed", stats);
            
        } catch (SQLException e) {
            return ServiceResult.error("Database error: " + e.getMessage());
        }
    }
    
    /**
     * MODIFIED: Get all grades for a section
     */
    public ServiceResult<List<StudentGradeRecord>> getSectionGrades(int instructorId, int sectionId) {
        try {
            if (!isMySection(instructorId, sectionId)) {
                return ServiceResult.error("Access denied: This is not your section");
            }
            
            List<Enrollment> enrollments = erpDb.getSectionEnrollmentsAll(sectionId);
            List<StudentGradeRecord> records = new ArrayList<>();
            
            for (Enrollment enrollment : enrollments) {
                var studentOpt = erpDb.getStudentById(enrollment.getStudentId());
                if (studentOpt.isEmpty()) {
                    continue;
                }
                
                Student student = studentOpt.get();
                int userId = student.getUserId();
                
                // Get grade for this student in this section
                var gradeOpt = erpDb.getGradeByUserAndSection(userId, sectionId);
                
                StudentGradeRecord record = new StudentGradeRecord();
                record.setEnrollmentId(enrollment.getEnrollmentId());
                record.setStudentId(enrollment.getStudentId());
                record.setUserId(userId);
                record.setRollNo(student.getRollNo());
                record.setGrade(gradeOpt.orElse(null));
                record.setFinalGrade(enrollment.getFinalGrade());
                
                records.add(record);
            }
            
            return ServiceResult.success("Grades retrieved", records);
            
        } catch (SQLException e) {
            return ServiceResult.error("Database error: " + e.getMessage());
        }
    }
    
    /**
     * Gets instructor profile by user ID
     */
    public Instructor getInstructorByUserId(int userId) throws SQLException {
        return erpDb.getInstructorByUserId(userId).orElse(null);
    }
    
    /**
     * Gets instructor profile by instructor ID
     */
    public Instructor getInstructorById(int instructorId) throws SQLException {
        return erpDb.getInstructorById(instructorId).orElse(null);
    }
    
    // ==================== INNER CLASSES ====================
    
    /**
     * MODIFIED: Updated to work with new Grade format
     */
    public static class StudentGradeRecord {
        private int enrollmentId;
        private int studentId;
        private int userId;
        private String rollNo;
        private Grade grade;  // Single Grade object with quiz, midsem, endsem
        private String finalGrade;
        
        public int getEnrollmentId() { return enrollmentId; }
        public void setEnrollmentId(int enrollmentId) { this.enrollmentId = enrollmentId; }
        
        public int getStudentId() { return studentId; }
        public void setStudentId(int studentId) { this.studentId = studentId; }
        
        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }
        
        public String getRollNo() { return rollNo; }
        public void setRollNo(String rollNo) { this.rollNo = rollNo; }
        
        public Grade getGrade() { return grade; }
        public void setGrade(Grade grade) { this.grade = grade; }
        
        public String getFinalGrade() { return finalGrade; }
        public void setFinalGrade(String finalGrade) { this.finalGrade = finalGrade; }
    }
    
    public static class ClassStatistics {
        private int totalStudents;
        private int gradedStudents;
        private double averageScore;
        private double minScore;
        private double maxScore;
        private Map<String, Integer> gradeDistribution;
        
        public int getTotalStudents() { return totalStudents; }
        public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }
        
        public int getGradedStudents() { return gradedStudents; }
        public void setGradedStudents(int gradedStudents) { this.gradedStudents = gradedStudents; }
        
        public double getAverageScore() { return averageScore; }
        public void setAverageScore(double averageScore) { this.averageScore = averageScore; }
        
        public double getMinScore() { return minScore; }
        public void setMinScore(double minScore) { this.minScore = minScore; }
        
        public double getMaxScore() { return maxScore; }
        public void setMaxScore(double maxScore) { this.maxScore = maxScore; }
        
        public Map<String, Integer> getGradeDistribution() { return gradeDistribution; }
        public void setGradeDistribution(Map<String, Integer> gradeDistribution) { 
            this.gradeDistribution = gradeDistribution; 
        }
    }
}
