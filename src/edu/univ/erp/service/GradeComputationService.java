package edu.univ.erp.service;

import edu.univ.erp.data.ERPDatabase;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Grade;
import edu.univ.erp.service.InstructorService.ClassStatistics;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GradeComputationService - Handles grade computations and statistics
 * MODIFIED: Updated to work with 10-point GPA scale
 */
public class GradeComputationService {
    
    /**
     * Converts numerical grade to letter grade
     */
    public String getLetterGrade(double score) {
        if (score >= 90) return "A+";
        if (score >= 85) return "A";
        if (score >= 80) return "A-";
        if (score >= 75) return "B+";
        if (score >= 70) return "B";
        if (score >= 65) return "B-";
        if (score >= 60) return "C+";
        if (score >= 55) return "C";
        if (score >= 50) return "C-";
        if (score >= 45) return "D";
        return "F";
    }
    
    /**
     * Gets GPA value for letter grade (10-POINT SCALE)
     */
    public double getGPA(String letterGrade) {
        if (letterGrade == null) return 0.0;
        switch (letterGrade) {
            case "A+": return 10.0;
            case "A":  return 9.0;
            case "A-": return 8.5;
            case "B+": return 8.0;
            case "B":  return 7.0;
            case "B-": return 6.5;
            case "C+": return 6.0;
            case "C":  return 5.5;
            case "C-": return 5.0;
            case "D":  return 4.0;
            case "F":  return 0.0;
            default: return 0.0;
        }
    }
    
    /**
     * MODIFIED: Compute class statistics using new grade format
     */
    public ClassStatistics computeStatisticsNew(List<Enrollment> enrollments, List<Grade> grades, ERPDatabase erpDb) 
            throws SQLException {
        ClassStatistics stats = new ClassStatistics();
        
        double totalScore = 0.0;
        int count = 0;
        double minScore = Double.MAX_VALUE;
        double maxScore = Double.MIN_VALUE;
        
        Map<String, Integer> gradeDistribution = new HashMap<>();
        gradeDistribution.put("A+", 0);
        gradeDistribution.put("A", 0);
        gradeDistribution.put("A-", 0);
        gradeDistribution.put("B+", 0);
        gradeDistribution.put("B", 0);
        gradeDistribution.put("B-", 0);
        gradeDistribution.put("C+", 0);
        gradeDistribution.put("C", 0);
        gradeDistribution.put("C-", 0);
        gradeDistribution.put("D", 0);
        gradeDistribution.put("F", 0);
        gradeDistribution.put("N/A", 0);
        
        // Create map of userId to Grade for quick lookup
        Map<Integer, Grade> gradeMap = new HashMap<>();
        for (Grade grade : grades) {
            gradeMap.put(grade.getUserId(), grade);
        }
        
        for (Enrollment enrollment : enrollments) {
            // Get student's user_id
            var studentOpt = erpDb.getStudentById(enrollment.getStudentId());
            if (studentOpt.isEmpty()) {
                continue;
            }
            
            int userId = studentOpt.get().getUserId();
            Grade grade = gradeMap.get(userId);
            
            String finalGrade = enrollment.getFinalGrade();
            
            if (grade != null && grade.hasAllGrades()) {
                double score = grade.getTotalScore();
                
                totalScore += score;
                count++;
                
                if (score < minScore) minScore = score;
                if (score > maxScore) maxScore = score;
            }
            
            // Count grade distribution
            if (finalGrade != null && !finalGrade.isEmpty()) {
                gradeDistribution.put(finalGrade, gradeDistribution.getOrDefault(finalGrade, 0) + 1);
            } else {
                gradeDistribution.put("N/A", gradeDistribution.get("N/A") + 1);
            }
        }
        
        stats.setTotalStudents(enrollments.size());
        stats.setGradedStudents(count);
        stats.setAverageScore(count > 0 ? totalScore / count : 0.0);
        stats.setMinScore(count > 0 ? minScore : 0.0);
        stats.setMaxScore(count > 0 ? maxScore : 0.0);
        stats.setGradeDistribution(gradeDistribution);
        
        return stats;
    }
    
    /**
     * Computes CGPA for a student (10-POINT SCALE)
     */
    public double computeCGPA(List<StudentService.StudentGradeInfo> gradeInfoList) {
        double totalGradePoints = 0.0;
        int totalCredits = 0;
        
        for (StudentService.StudentGradeInfo info : gradeInfoList) {
            String letterGrade = info.getFinalGrade();
            
            if (letterGrade != null && !letterGrade.isEmpty()) {
                int credits = info.getEnrollment().getCredits();
                double gpa = getGPA(letterGrade);
                
                totalGradePoints += gpa * credits;
                totalCredits += credits;
            }
        }
        
        return totalCredits > 0 ? totalGradePoints / totalCredits : 0.0;
    }
}
