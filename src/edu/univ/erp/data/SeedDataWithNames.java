package edu.univ.erp.data;

import org.mindrot.jbcrypt.BCrypt;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import edu.univ.erp.domain.*;

import edu.univ.erp.service.AuthService;


/**
 * Complete Seed Data Script with Names
 * MODIFIED: Updated to work with new grades table format (user_id, section_id, quiz, midsem, endsem)
 */
public class SeedDataWithNames {
    
    public static void seedCompleteData(AuthDatabase authDb, ERPDatabase erpDb) throws SQLException {
        System.out.println("\n===== SEEDING COMPLETE SAMPLE DATA =====\n");
        
        // 1. Create sample users with hashed passwords and emails
        int adminUserId = createAdmin(authDb);
        int instructor1UserId = createInstructor1(authDb);
        int instructor2UserId = createInstructor2(authDb);
        int student1UserId = createStudent1(authDb);
        int student2UserId = createStudent2(authDb);
        int student3UserId = createStudent3(authDb);
        
        // 2. Create instructor profiles
        int instructor1Id = createInstructorProfile(erpDb, instructor1UserId, "EMP001", "Rajesh", "Sharma", 
                                                     "Computer Science", "Professor", 
                                                     "r.sharma@univ.edu", "+91-9876543210", "Room 301");
        int instructor2Id = createInstructorProfile(erpDb, instructor2UserId, "EMP002", "Priya", "Kumar", 
                                                     "Mathematics", "Associate Professor", 
                                                     "p.kumar@univ.edu", "+91-9876543211", "Room 205");
        
        // 3. Create student profiles with names
        int student1Id = createStudentProfile(erpDb, student1UserId, "2021CS001", "Amit", "Patel", 
                                              "B.Tech Computer Science", 3, 
                                              "amit.patel@student.univ.edu", "+91-9123456789", 
                                              "123, Student Hostel A");
        int student2Id = createStudentProfile(erpDb, student2UserId, "2021CS002", "Neha", "Sharma", 
                                              "B.Tech Computer Science", 3, 
                                              "neha.sharma@student.univ.edu", "+91-9123456790", 
                                              "124, Student Hostel A");
        int student3Id = createStudentProfile(erpDb, student3UserId, "2021CS003", "Rahul", "Verma", 
                                              "B.Tech Computer Science", 3, 
                                              "rahul.verma@student.univ.edu", "+91-9123456791", 
                                              "125, Student Hostel A");
        
        // 4. Create courses
        createCourses(erpDb);
        
        // 5. Create sections for Monsoon 2025 semester
        createSections(erpDb, instructor1Id, instructor2Id);
        
        // 6. Create enrollments
        createStudent1Enrollments(erpDb, student1Id);
        createStudent2Enrollments(erpDb, student2Id);
        createStudent3Enrollments(erpDb, student3Id);
        
        // 7. Add grades for students (using new format)
        addGradesForStudent1(erpDb, student1UserId);
        addGradesForStudent2(erpDb, student2UserId);
        addGradesForStudent3(erpDb, student3UserId);
        
        System.out.println("\n===== SEED DATA COMPLETED SUCCESSFULLY =====");
        System.out.println("Sample Accounts Created:");
        System.out.println("  Admin: admin1 / Admin@123 (admin@univ.edu)");
        System.out.println("  Instructor 1: dr.sharma / instructor123 (r.sharma@univ.edu)");
        System.out.println("  Instructor 2: prof.kumar / instructor123 (p.kumar@univ.edu)");
        System.out.println("  Student 1: stu001 / student123 (amit.patel@student.univ.edu)");
        System.out.println("  Student 2: stu002 / student123 (neha.sharma@student.univ.edu)");
        System.out.println("  Student 3: stu003 / student123 (rahul.verma@student.univ.edu)");
    }
    
    private static int createAdmin(AuthDatabase authDb) throws SQLException {
        if (!authDb.usernameExists("admin1")) {
            String passwordHash = AuthService.hashPassword("Admin@123");
            int userId = authDb.addUser("admin1", AuthDatabase.ROLE_ADMIN, passwordHash, "admin@univ.edu");
            System.out.println("✓ Created Admin: admin1 (User ID: " + userId + ", Email: admin@univ.edu)");
            return userId;
        }
        System.out.println("✓ Admin1 already exists");
        return authDb.findByUsername("admin1").get().getUserId();
    }
    
    private static int createInstructor1(AuthDatabase authDb) throws SQLException {
        if (!authDb.usernameExists("dr.sharma")) {
            String passwordHash = AuthService.hashPassword("instructor123");
            int userId = authDb.addUser("dr.sharma", AuthDatabase.ROLE_INSTRUCTOR, passwordHash, "r.sharma@univ.edu");
            System.out.println("✓ Created Instructor 1: dr.sharma (User ID: " + userId + ", Email: r.sharma@univ.edu)");
            return userId;
        }
        System.out.println("✓ Instructor 1 already exists");
        return authDb.findByUsername("dr.sharma").get().getUserId();
    }
    
    private static int createInstructor2(AuthDatabase authDb) throws SQLException {
        if (!authDb.usernameExists("prof.kumar")) {
            String passwordHash = AuthService.hashPassword("instructor123");
            int userId = authDb.addUser("prof.kumar", AuthDatabase.ROLE_INSTRUCTOR, passwordHash, "p.kumar@univ.edu");
            System.out.println("✓ Created Instructor 2: prof.kumar (User ID: " + userId + ", Email: p.kumar@univ.edu)");
            return userId;
        }
        System.out.println("✓ Instructor 2 already exists");
        return authDb.findByUsername("prof.kumar").get().getUserId();
    }
    
    private static int createStudent1(AuthDatabase authDb) throws SQLException {
        if (!authDb.usernameExists("stu001")) {
            String passwordHash = AuthService.hashPassword("student123");
            int userId = authDb.addUser("stu001", AuthDatabase.ROLE_STUDENT, passwordHash, "amit.patel@student.univ.edu");
            System.out.println("✓ Created Student 1: stu001 (User ID: " + userId + ", Email: amit.patel@student.univ.edu)");
            return userId;
        }
        System.out.println("✓ Student 1 already exists");
        return authDb.findByUsername("stu001").get().getUserId();
    }
    
    private static int createStudent2(AuthDatabase authDb) throws SQLException {
        if (!authDb.usernameExists("stu002")) {
            String passwordHash = AuthService.hashPassword("student123");
            int userId = authDb.addUser("stu002", AuthDatabase.ROLE_STUDENT, passwordHash, "neha.sharma@student.univ.edu");
            System.out.println("✓ Created Student 2: stu002 (User ID: " + userId + ", Email: neha.sharma@student.univ.edu)");
            return userId;
        }
        System.out.println("✓ Student 2 already exists");
        return authDb.findByUsername("stu002").get().getUserId();
    }
    
    private static int createStudent3(AuthDatabase authDb) throws SQLException {
        if (!authDb.usernameExists("stu003")) {
            String passwordHash = AuthService.hashPassword("student123");
            int userId = authDb.addUser("stu003", AuthDatabase.ROLE_STUDENT, passwordHash, "rahul.verma@student.univ.edu");
            System.out.println("✓ Created Student 3: stu003 (User ID: " + userId + ", Email: rahul.verma@student.univ.edu)");
            return userId;
        }
        System.out.println("✓ Student 3 already exists");
        return authDb.findByUsername("stu003").get().getUserId();
    }
    
    private static int createInstructorProfile(ERPDatabase erpDb, int userId, String empId, 
                                               String firstName, String lastName, String dept, 
                                               String designation, String email, String phone, String office) 
                                               throws SQLException {
        if (erpDb.getInstructorByUserId(userId).isPresent()) {
            int instructorId = erpDb.getInstructorByUserId(userId).get().getInstructorId();
            System.out.println("✓ Instructor Profile already exists: " + firstName + " " + lastName + " (" + empId + ")");
            return instructorId;
        }
        
        int instructorId = erpDb.addInstructor(userId, empId, firstName, lastName, dept, designation, email, phone, office);
        System.out.println("✓ Created Instructor Profile: " + firstName + " " + lastName + " (" + empId + ") - ID: " + instructorId);
        return instructorId;
    }
    
    private static int createStudentProfile(ERPDatabase erpDb, int userId, String rollNo, 
                                           String firstName, String lastName, String program, 
                                           int year, String email, String phone, String address) 
                                           throws SQLException {
        if (erpDb.getStudentByUserId(userId).isPresent()) {
            int studentId = erpDb.getStudentByUserId(userId).get().getStudentId();
            System.out.println("✓ Student Profile already exists: " + firstName + " " + lastName + " (" + rollNo + ")");
            return studentId;
        }
        
        int studentId = erpDb.addStudent(userId, rollNo, firstName, lastName, program, year, email, phone, address);
        System.out.println("✓ Created Student Profile: " + firstName + " " + lastName + " (" + rollNo + ") - ID: " + studentId);
        return studentId;
    }
    
    private static void createCourses(ERPDatabase erpDb) throws SQLException {
        System.out.println("\nCreating Courses...");
        
        // Monsoon 2025 Semester (Current - 3rd Year)
        createCourseIfNotExists(erpDb, "CS301", "Database Systems", 4, 
                                "Relational databases and SQL");
        createCourseIfNotExists(erpDb, "CS302", "Operating Systems", 4, 
                                "Process management and memory");
        createCourseIfNotExists(erpDb, "CS303", "Algorithms", 4, 
                                "Algorithm design and analysis");
        createCourseIfNotExists(erpDb, "MGMT301", "Project Management", 3, 
                                "Software project planning");
        
        System.out.println("✓ Courses created successfully");
    }
    
    private static void createCourseIfNotExists(ERPDatabase erpDb, String code, String title, 
                                               int credits, String description) throws SQLException {
        if (erpDb.getCourseByCode(code).isEmpty()) {
            erpDb.addCourse(code, title, credits, description, null);
            System.out.println("  - " + code + ": " + title);
        }
    }
    
    private static void createSections(ERPDatabase erpDb, int instructor1Id, int instructor2Id) 
                                      throws SQLException {
        System.out.println("\nCreating Sections...");
        
        // Monsoon 2025 Sections (Current semester)
        createSectionIfNotExists(erpDb, "CS301", instructor1Id, "A", "Mon Wed 10:00-11:30", "CS-301", 40, "Monsoon", 2025);
        createSectionIfNotExists(erpDb, "CS302", instructor1Id, "A", "Tue Thu 10:00-11:30", "CS-302", 40, "Monsoon", 2025);
        createSectionIfNotExists(erpDb, "CS303", instructor1Id, "A", "Mon Wed 14:00-15:30", "CS-303", 40, "Monsoon", 2025);
        createSectionIfNotExists(erpDb, "MGMT301", instructor2Id, "A", "Fri 10:00-13:00", "MGMT-301", 40, "Monsoon", 2025);
        
        System.out.println("✓ Sections created successfully");
    }
    
    private static void createSectionIfNotExists(ERPDatabase erpDb, String courseCode, int instructorId,
                                                 String sectionName, String dayTime, String room,
                                                 int capacity, String semester, int year) throws SQLException {
        var courseOpt = erpDb.getCourseByCode(courseCode);
        if (courseOpt.isEmpty()) {
            System.out.println("  ! Warning: Course " + courseCode + " not found, skipping section");
            return;
        }
        
        int courseId = courseOpt.get().getCourseId();
        
        var sections = erpDb.getSectionsBySemesterYear(semester, year);
        boolean exists = sections.stream()
            .anyMatch(s -> s.getCourseId() == courseId && s.getSectionName().equals(sectionName));
        
        if (!exists) {
            erpDb.addSection(courseId, instructorId, sectionName, dayTime, room, capacity, semester, year);
            System.out.println("  - " + courseCode + " Section " + sectionName + " (" + semester + " " + year + ")");
        }
    }
    
    private static void createStudent1Enrollments(ERPDatabase erpDb, int studentId) 
                                                  throws SQLException {
        System.out.println("\nCreating Enrollments for Student 1 (Amit Patel)...");
        
        // Monsoon 2025 - All courses
        enrollInCourse(erpDb, studentId, "CS301", "A", "Monsoon", 2025);
        enrollInCourse(erpDb, studentId, "CS302", "A", "Monsoon", 2025);
        enrollInCourse(erpDb, studentId, "CS303", "A", "Monsoon", 2025);
        enrollInCourse(erpDb, studentId, "MGMT301", "A", "Monsoon", 2025);
        
        System.out.println("✓ Enrollments created successfully");
    }
    
    private static void createStudent2Enrollments(ERPDatabase erpDb, int studentId) 
                                                  throws SQLException {
        System.out.println("\nCreating Enrollments for Student 2 (Neha Sharma)...");
        
        // Monsoon 2025 - All courses
        enrollInCourse(erpDb, studentId, "CS301", "A", "Monsoon", 2025);
        enrollInCourse(erpDb, studentId, "CS302", "A", "Monsoon", 2025);
        enrollInCourse(erpDb, studentId, "CS303", "A", "Monsoon", 2025);
        enrollInCourse(erpDb, studentId, "MGMT301", "A", "Monsoon", 2025);
        
        System.out.println("✓ Enrollments created successfully");
    }
    
    private static void createStudent3Enrollments(ERPDatabase erpDb, int studentId) 
                                                  throws SQLException {
        System.out.println("\nCreating Enrollments for Student 3 (Rahul Verma)...");
        
        // Monsoon 2025 - Partial enrollment
        enrollInCourse(erpDb, studentId, "CS301", "A", "Monsoon", 2025);
        enrollInCourse(erpDb, studentId, "CS302", "A", "Monsoon", 2025);
        
        System.out.println("✓ Enrollments created successfully");
    }
    
    private static void enrollInCourse(ERPDatabase erpDb, int studentId, String courseCode, 
                                      String sectionName, String semester, int year) throws SQLException {
        var courseOpt = erpDb.getCourseByCode(courseCode);
        if (courseOpt.isEmpty()) {
            return;
        }
        
        var sections = erpDb.getSectionsBySemesterYear(semester, year);
        var sectionOpt = sections.stream()
            .filter(s -> s.getCourseId() == courseOpt.get().getCourseId() && s.getSectionName().equals(sectionName))
            .findFirst();
        
        if (sectionOpt.isPresent()) {
            int sectionId = sectionOpt.get().getSectionId();
            
            if (!erpDb.isStudentEnrolled(studentId, sectionId)) {
                erpDb.enrollStudent(studentId, sectionId);
                System.out.println("  - Enrolled in " + courseCode + " Section " + sectionName + " (" + semester + " " + year + ")");
            }
        }
    }
    
    /**
     * MODIFIED: Add grades using new format (user_id, section_id, quiz, midsem, endsem)
     */
    private static void addGradesForStudent1(ERPDatabase erpDb, int userId) throws SQLException {
        System.out.println("\nAdding Grades for Student 1 (Amit Patel)...");
        
        // Monsoon 2025 - Partial grades (quiz and midsem only)
        addGradesForCourse(erpDb, userId, "CS301", "Monsoon", 2025, 18.0, 27.0, null);
        addGradesForCourse(erpDb, userId, "CS302", "Monsoon", 2025, 17.0, 26.0, null);
        addGradesForCourse(erpDb, userId, "CS303", "Monsoon", 2025, 19.0, 28.0, null);
        addGradesForCourse(erpDb, userId, "MGMT301", "Monsoon", 2025, 18.0, 27.0, null);
        
        System.out.println("✓ Grades added successfully");
    }
    
    private static void addGradesForStudent2(ERPDatabase erpDb, int userId) throws SQLException {
        System.out.println("\nAdding Grades for Student 2 (Neha Sharma)...");
        
        // Monsoon 2025 - Partial grades (quiz and midsem only)
        addGradesForCourse(erpDb, userId, "CS301", "Monsoon", 2025, 17.0, 26.0, null);
        addGradesForCourse(erpDb, userId, "CS302", "Monsoon", 2025, 18.0, 27.0, null);
        addGradesForCourse(erpDb, userId, "CS303", "Monsoon", 2025, 16.0, 25.0, null);
        addGradesForCourse(erpDb, userId, "MGMT301", "Monsoon", 2025, 17.0, 26.0, null);
        
        System.out.println("✓ Grades added successfully");
    }
    
    private static void addGradesForStudent3(ERPDatabase erpDb, int userId) throws SQLException {
        System.out.println("\nAdding Grades for Student 3 (Rahul Verma)...");
        
        // Monsoon 2025 - Minimal grades (only quiz for enrolled courses)
        addGradesForCourse(erpDb, userId, "CS301", "Monsoon", 2025, 15.0, null, null);
        addGradesForCourse(erpDb, userId, "CS302", "Monsoon", 2025, 16.0, null, null);
        
        System.out.println("✓ Grades added successfully");
    }
    
    /**
     * MODIFIED: Add grades using new saveOrUpdateGrade method
     */
    private static void addGradesForCourse(ERPDatabase erpDb, int userId, String courseCode,
                                          String semester, int year, Double quiz, 
                                          Double midsem, Double endsem) throws SQLException {
        // Get section
        var courseOpt = erpDb.getCourseByCode(courseCode);
        if (courseOpt.isEmpty()) {
            return;
        }
        
        var sections = erpDb.getSectionsBySemesterYear(semester, year);
        var sectionOpt = sections.stream()
            .filter(s -> s.getCourseId() == courseOpt.get().getCourseId())
            .findFirst();
        
        if (sectionOpt.isEmpty()) {
            return;
        }
        
        int sectionId = sectionOpt.get().getSectionId();
        
        // Check if grade already exists
        var existingGrade = erpDb.getGradeByUserAndSection(userId, sectionId);
        if (existingGrade.isPresent()) {
            return; // Grade already exists, skip
        }
        
        // Save grade
        erpDb.saveOrUpdateGrade(userId, sectionId, quiz, midsem, endsem);
        
        // If all grades are present, compute and save final grade
        if (quiz != null && midsem != null && endsem != null) {
            double totalPercentage = quiz + midsem + endsem;
            String letterGrade = getLetterGrade(totalPercentage);
            
            // Get enrollment ID to update final grade
            var student = erpDb.getStudentByUserId(userId);
            if (student.isPresent()) {
                int studentId = student.get().getStudentId();
                List<Enrollment> enrollments = erpDb.getStudentEnrollments(studentId, null);
                for (var enrollment : enrollments) {
                    if (enrollment.getSectionId() == sectionId) {
                        erpDb.updateFinalGrade(enrollment.getEnrollmentId(), letterGrade);
                        break;
                    }
                }
            }
            
            System.out.println("  - Added complete grades for " + courseCode + " (" + semester + " " + year + "): " + letterGrade);
        } else {
            System.out.println("  - Added partial grades for " + courseCode + " (" + semester + " " + year + ")");
        }
    }
    
    private static String getLetterGrade(double percentage) {
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
}
