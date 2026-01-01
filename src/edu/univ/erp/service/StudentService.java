package edu.univ.erp.service;

import edu.univ.erp.data.ERPDatabase;
import edu.univ.erp.domain.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.HashSet;
import java.util.Set;

/**
 * StudentService - Handles all student-related operations
 * UPDATED: 
 * 1. Changed clash detection algorithm using regex for new format: Mon/Tue 11:00-12:00
 * 2. Edge times (e.g., 11:00-12:00 and 12:00-13:00) are considered valid (no clash)
 * 3. Added drop deadline validation
 */
public class StudentService {
    
    private final ERPDatabase erpDb;
    private final AccessControlService accessControl;
    
    public StudentService(ERPDatabase erpDb, AccessControlService accessControl) {
        this.erpDb = erpDb;
        this.accessControl = accessControl;
    }
    
    /**
     * Registers a student for a section
     * Checks: status, capacity, time clash, drop deadline (for registration validation)
     */
    public ServiceResult registerForSection(int studentId, int sectionId) {
        try {
            // Check maintenance mode
            String validationError = accessControl.validateOperation("STUDENT", true);
            if (validationError != null) {
                return ServiceResult.error(validationError);
            }
            
            // Get section details
            var sectionOpt = erpDb.getSectionById(sectionId);
            if (sectionOpt.isEmpty()) {
                return ServiceResult.error("Section not found");
            }
            
            Section section = sectionOpt.get();
            
            // Check if already enrolled
            if (erpDb.isStudentEnrolled(studentId, sectionId)) {
                return ServiceResult.error("You are already registered for this section");
            }
            
            // Check section status
            if (section.isClosed()) {
                return ServiceResult.error("Section is closed. Registration not available.");
            }
            
            // Check capacity
            if (!section.hasSeats()) {
                return ServiceResult.error("Section is full (Capacity: " + section.getCapacity() + ")");
            }
            
            // Check for time clashes with existing enrollments
            List<Enrollment> activeEnrollments = erpDb.getStudentEnrollments(studentId, ERPDatabase.ENROLLMENT_ACTIVE);
            String newDayTime = section.getDayTime();
            
            if (newDayTime != null && !newDayTime.isEmpty() && !newDayTime.equalsIgnoreCase("TBA")) {
                for (Enrollment enrollment : activeEnrollments) {
                    // Get the enrolled section details
                    var enrolledSectionOpt = erpDb.getSectionById(enrollment.getSectionId());
                    if (enrolledSectionOpt.isPresent()) {
                        Section enrolledSection = enrolledSectionOpt.get();
                        String existingDayTime = enrolledSection.getDayTime();
                        
                        if (existingDayTime != null && !existingDayTime.isEmpty() && 
                            !existingDayTime.equalsIgnoreCase("TBA")) {
                            // Check for time clash using NEW algorithm
                            if (hasTimeClash(newDayTime, existingDayTime)) {
                                return ServiceResult.error(
                                    "Time clash detected! You already have " + 
                                    enrolledSection.getCourseCode() + " at " + existingDayTime + 
                                    ". Cannot register for course " + section.getCourseCode()
                                );
                            }
                        }
                    }
                }
            }
            
            // Register student
            int enrollmentId = erpDb.enrollStudent(studentId, sectionId);
            
            return ServiceResult.success("Successfully registered for " + section.getCourseCode() + 
                                       " - " + section.getCourseTitle(), enrollmentId);
            
        } catch (SQLException e) {
            return ServiceResult.error("Database error: " + e.getMessage());
        }
    }
    
    /**
     * UPDATED: Helper method to detect time clashes between two course schedules
     * NEW FORMAT: "Mon/Wed/Fri 10:00-11:30"
     * Uses regex to parse days (separated by /) and times (separated by -)
     * Edge case: 11:00-12:00 and 12:00-13:00 are NOT considered clashing
     */
    private boolean hasTimeClash(String time1, String time2) {
        try {
            // Parse day and time information using regex
            TimeSlot slot1 = parseTimeSlotWithRegex(time1);
            TimeSlot slot2 = parseTimeSlotWithRegex(time2);
            
            if (slot1 == null || slot2 == null) {
                return false; // Cannot determine, allow registration
            }
            
            // Check if any days overlap
            for (String day1 : slot1.days) {
                for (String day2 : slot2.days) {
                    if (day1.equalsIgnoreCase(day2)) {
                        // Same day, check time overlap
                        // IMPORTANT: Edge times should NOT clash
                        // e.g., 11:00-12:00 and 12:00-13:00 are valid
                        if (timeRangesOverlapExcludingEdges(slot1.startTime, slot1.endTime, 
                                                            slot2.startTime, slot2.endTime)) {
                            return true; // Time clash detected
                        }
                    }
                }
            }
            
            return false;
        } catch (Exception e) {
            // If parsing fails, return false to allow registration
            return false;
        }
    }
    
    /**
     * NEW: Parses a time slot string using regex
     * Format: "Mon/Wed/Fri 10:00-11:30"
     * Steps:
     * 1. Split on space to separate days and time
     * 2. Split days on "/" to get individual days
     * 3. Split time on "-" to get start and end times
     */
    private TimeSlot parseTimeSlotWithRegex(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            return null;
        }
        
        try {
            TimeSlot slot = new TimeSlot();
            slot.days = new ArrayList<>();
            
            // Step 1: Split on whitespace to separate days from time
            // Regex: \s+ matches one or more whitespace characters
            String[] mainParts = timeString.trim().split("\\s+");
            
            if (mainParts.length < 2) {
                return null; // Invalid format
            }
            
            // First part(s) are days, last part is time
            // Combine all parts except the last one as days string
            StringBuilder daysBuilder = new StringBuilder();
            for (int i = 0; i < mainParts.length - 1; i++) {
                if (i > 0) daysBuilder.append(" ");
                daysBuilder.append(mainParts[i]);
            }
            String daysString = daysBuilder.toString();
            String timeRange = mainParts[mainParts.length - 1];
            
            // Step 2: Split days on "/" using regex
            // Regex: / is the separator
            String[] dayParts = daysString.split("/");
            
            for (String dayPart : dayParts) {
                String day = dayPart.trim();
                if (!day.isEmpty() && isDay(day)) {
                    slot.days.add(normalizeDayName(day));
                }
            }
            
            if (slot.days.isEmpty()) {
                return null; // No valid days found
            }
            
            // Step 3: Parse time range using regex
            // Regex: Split on "-" to get start and end times
            String[] timeParts = timeRange.split("-");
            
            if (timeParts.length != 2) {
                return null; // Invalid time format
            }
            
            slot.startTime = parseTime(timeParts[0].trim());
            slot.endTime = parseTime(timeParts[1].trim());
            
            if (slot.startTime < 0 || slot.endTime < 0) {
                return null;
            }
            
            return slot;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Checks if a string is a day name
     */
    private boolean isDay(String str) {
        if (str == null || str.isEmpty()) return false;
        // Remove any non-alphabetic characters using regex
        String clean = str.replaceAll("[^a-zA-Z]", "").toLowerCase();
        // Use regex pattern to match day names
        Pattern dayPattern = Pattern.compile("^(mon|monday|tue|tuesday|wed|wednesday|thu|thursday|fri|friday|sat|saturday|sun|sunday)$");
        return dayPattern.matcher(clean).matches();
    }
    
    /**
     * Normalizes day name to standard 3-letter format
     */
    private String normalizeDayName(String day) {
        if (day == null || day.isEmpty()) return day;
        String lower = day.replaceAll("[^a-zA-Z]", "").toLowerCase();
        if (lower.startsWith("mon")) return "Mon";
        if (lower.startsWith("tue")) return "Tue";
        if (lower.startsWith("wed")) return "Wed";
        if (lower.startsWith("thu")) return "Thu";
        if (lower.startsWith("fri")) return "Fri";
        if (lower.startsWith("sat")) return "Sat";
        if (lower.startsWith("sun")) return "Sun";
        return day;
    }
    
    /**
     * Parses time string like "10:00" or "14:30" to minutes since midnight
     * Uses regex to extract hours and minutes
     */
    private int parseTime(String timeStr) {
        try {
            timeStr = timeStr.trim();
            
            // Use regex to extract time components
            // Pattern: HH:MM format
            Pattern timePattern = Pattern.compile("(\\d{1,2}):(\\d{2})");
            Matcher matcher = timePattern.matcher(timeStr);
            
            if (!matcher.find()) {
                return -1;
            }
            
            int hours = Integer.parseInt(matcher.group(1));
            int minutes = Integer.parseInt(matcher.group(2));
            
            // Validate ranges
            if (hours < 0 || hours > 23 || minutes < 0 || minutes > 59) {
                return -1;
            }
            
            return hours * 60 + minutes;
        } catch (Exception e) {
            return -1;
        }
    }
    
    /**
     * UPDATED: Checks if two time ranges overlap (EXCLUDING edge cases)
     * Edge case: 11:00-12:00 and 12:00-13:00 should NOT overlap (valid)
     * 
     * Two ranges overlap if: start1 < end2 AND start2 < end1
     * But we exclude edge touching: start1 == end2 OR start2 == end1
     */
    private boolean timeRangesOverlapExcludingEdges(int start1, int end1, int start2, int end2) {
        // Standard overlap check
        boolean overlaps = start1 < end2 && start2 < end1;
        
        // Exclude edge cases where one ends exactly when the other starts
        boolean touchingEdge = (end1 == start2) || (end2 == start1);
        
        // Overlap only if they intersect AND not just touching at edges
        return overlaps && !touchingEdge;
    }
    
    /**
     * Inner class to hold parsed time slot information
     */
    private static class TimeSlot {
        List<String> days;
        int startTime;  // Minutes since midnight
        int endTime;    // Minutes since midnight
    }
    
    /**
     * UPDATED: Drops a section for a student
     * NEW: Check drop deadline before allowing drop
     */
    public ServiceResult dropSection(int studentId, int sectionId) {
        try {
            // Check maintenance mode
            String validationError = accessControl.validateOperation("STUDENT", true);
            if (validationError != null) {
                return ServiceResult.error(validationError);
            }
            
            // Get enrollment
            List<Enrollment> enrollments = erpDb.getStudentEnrollments(studentId, ERPDatabase.ENROLLMENT_ACTIVE);
            Enrollment targetEnrollment = null;
            
            for (Enrollment e : enrollments) {
                if (e.getSectionId() == sectionId) {
                    targetEnrollment = e;
                    break;
                }
            }
            
            if (targetEnrollment == null) {
                return ServiceResult.error("You are not enrolled in this section");
            }
            
            // NEW: Check drop deadline
            var sectionOpt = erpDb.getSectionById(sectionId);
            if (sectionOpt.isPresent()) {
                Section section = sectionOpt.get();
                if (!section.canDrop()) {
                    return ServiceResult.error("Cannot drop this section. Drop deadline has passed (" + 
                                             section.getDropDeadline() + ")");
                }
            }
            
            // Drop the section
            erpDb.dropEnrollment(targetEnrollment.getEnrollmentId());
            
            return ServiceResult.success("Successfully dropped " + targetEnrollment.getCourseCode());
            
        } catch (SQLException e) {
            return ServiceResult.error("Database error: " + e.getMessage());
        }
    }
    
    /**
     * Gets student's active enrollments (timetable)
     */
    public List<Enrollment> getStudentTimetable(int studentId) throws SQLException {
        return erpDb.getStudentEnrollments(studentId, ERPDatabase.ENROLLMENT_ACTIVE);
    }
    
    /**
     * Gets all enrollments for a student
     */
    public List<Enrollment> getAllEnrollments(int studentId) throws SQLException {
        return erpDb.getStudentEnrollments(studentId, null);
    }
    
    /**
     * Gets active enrollments only
     */
    public List<Enrollment> getActiveEnrollments(int studentId) throws SQLException {
        return erpDb.getStudentEnrollments(studentId, ERPDatabase.ENROLLMENT_ACTIVE);
    }
    
    /**
     * Gets grade for a student's enrollment using new format
     */
    public Grade getEnrollmentGrade(int enrollmentId) throws SQLException {
        // Get enrollment to find student's user_id and section_id
        var enrollmentOpt = erpDb.getEnrollmentById(enrollmentId);
        if (enrollmentOpt.isEmpty()) {
            return null;
        }
        
        Enrollment enrollment = enrollmentOpt.get();
        int sectionId = enrollment.getSectionId();
        
        // Get student to get user_id
        var studentOpt = erpDb.getStudentById(enrollment.getStudentId());
        if (studentOpt.isEmpty()) {
            return null;
        }
        
        int userId = studentOpt.get().getUserId();
        
        // Get grade using new format
        return erpDb.getGradeByUserAndSection(userId, sectionId).orElse(null);
    }
    
    /**
     * Gets all grades for a student with course details
     */
    public List<StudentGradeInfo> getAllGradesWithDetails(int studentId) throws SQLException {
        List<Enrollment> enrollments = erpDb.getStudentEnrollments(studentId, null);
        List<StudentGradeInfo> gradeInfoList = new ArrayList<>();
        
        // Get student to get user_id
        var studentOpt = erpDb.getStudentById(studentId);
        if (studentOpt.isEmpty()) {
            return gradeInfoList;
        }
        
        int userId = studentOpt.get().getUserId();
        
        for (Enrollment enrollment : enrollments) {
            int sectionId = enrollment.getSectionId();
            
            // Get grade using new format (user_id, section_id)
            Grade grade = erpDb.getGradeByUserAndSection(userId, sectionId).orElse(null);
            
            StudentGradeInfo info = new StudentGradeInfo();
            info.setEnrollment(enrollment);
            info.setGrade(grade);  // Single Grade object with quiz, midsem, endsem
            info.setFinalGrade(enrollment.getFinalGrade());
            
            gradeInfoList.add(info);
        }
        
        return gradeInfoList;
    }
    
    /**
     * Gets student profile by user ID
     */
    public Student getStudentByUserId(int userId) throws SQLException {
        return erpDb.getStudentByUserId(userId).orElse(null);
    }
    
    /**
     * Gets student profile by student ID
     */
    public Student getStudentById(int studentId) throws SQLException {
        return erpDb.getStudentById(studentId).orElse(null);
    }
    
    /**
     * Updates student profile
     * UPDATED: Added maintenance mode check
     */
    public ServiceResult updateProfile(int studentId, String firstName, String lastName, String program, int year, 
                                      String email, String phone, String address) {
        try {
            // Check maintenance mode
            String validationError = accessControl.validateOperation("STUDENT", true);
            if (validationError != null) {
                return ServiceResult.error(validationError);
            }
            
            erpDb.updateStudent(studentId, firstName, lastName, program, year, email, phone, address);
            return ServiceResult.success("Profile updated successfully");
        } catch (SQLException e) {
            return ServiceResult.error("Failed to update profile: " + e.getMessage());
        }
    }
    
    /**
     * Gets transcript data for a student
     */
    public List<TranscriptRecord> getTranscript(int studentId) throws SQLException {
        List<Enrollment> enrollments = erpDb.getStudentEnrollments(studentId, ERPDatabase.ENROLLMENT_COMPLETED);
        List<TranscriptRecord> records = new ArrayList<>();
        
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getFinalGrade() != null) {
                TranscriptRecord record = new TranscriptRecord();
                record.setCourseCode(enrollment.getCourseCode());
                record.setCourseTitle(enrollment.getCourseTitle());
                record.setCredits(enrollment.getCredits());
                record.setGrade(enrollment.getFinalGrade());
                record.setSemester(enrollment.getSemester());
                record.setYear(enrollment.getYear());
                
                records.add(record);
            }
        }
        
        return records;
    }
    
    // ==================== INNER CLASSES ====================
    
    /**
     * Updated to work with new Grade format
     */
    public static class StudentGradeInfo {
        private Enrollment enrollment;
        private Grade grade;  // Single Grade object with quiz, midsem, endsem
        private String finalGrade;
        
        public Enrollment getEnrollment() { return enrollment; }
        public void setEnrollment(Enrollment enrollment) { this.enrollment = enrollment; }
        
        public Grade getGrade() { return grade; }
        public void setGrade(Grade grade) { this.grade = grade; }
        
        public String getFinalGrade() { return finalGrade; }
        public void setFinalGrade(String finalGrade) { this.finalGrade = finalGrade; }
    }
    
    public static class TranscriptRecord {
        private String courseCode;
        private String courseTitle;
        private int credits;
        private String grade;
        private String semester;
        private int year;
        
        public String getCourseCode() { return courseCode; }
        public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
        
        public String getCourseTitle() { return courseTitle; }
        public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }
        
        public int getCredits() { return credits; }
        public void setCredits(int credits) { this.credits = credits; }
        
        public String getGrade() { return grade; }
        public void setGrade(String grade) { this.grade = grade; }
        
        public String getSemester() { return semester; }
        public void setSemester(String semester) { this.semester = semester; }
        
        public int getYear() { return year; }
        public void setYear(int year) { this.year = year; }
    }
}
