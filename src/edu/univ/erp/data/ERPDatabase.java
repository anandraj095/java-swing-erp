package edu.univ.erp.data;


import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.Admin;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ERPDatabase - Handles all operations for the ERP Database
 * Manages: students, instructors, admins, courses, sections, enrollments, grades, settings
 * MODIFIED: Changed grades table structure to user_id, section_id, quiz, midsem, endsem format
 * FIXED: Removed UNIQUE constraint from course_id in sections table to allow multiple sections per course
 * FIXED: Changed drop_deadline from DATE to DATETIME to store both date and time
 */
public class ERPDatabase {
    
    private final Connection connection;
    
    // Enrollment status constants
    public static final String ENROLLMENT_ACTIVE = "ACTIVE";
    public static final String ENROLLMENT_DROPPED = "DROPPED";
    public static final String ENROLLMENT_COMPLETED = "COMPLETED";
    
    /**
     * Constructor - Initialize with an active database connection
     * @param connection Active JDBC connection to ERP DB
     */
    public ERPDatabase(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * Create all required tables if they don't exist
     * MODIFIED: Updated createGradesTable()
     */
    public void createTablesIfNotExist() throws SQLException {
        createStudentsTable();
        createInstructorsTable();
        createAdminsTable();
        createCoursesTable();
        createSectionsTable();
        createEnrollmentsTable();
        createGradesTable(); // MODIFIED
        createSettingsTable();
    }
    

    private void createStudentsTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS students (
                student_id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT UNIQUE NOT NULL,
                roll_no VARCHAR(20) UNIQUE NOT NULL,
                first_name VARCHAR(100) NOT NULL,
                last_name VARCHAR(100) NOT NULL,
                program VARCHAR(100) NOT NULL,
                year INT NOT NULL,
                email VARCHAR(100),
                phone VARCHAR(20),
                address TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                INDEX idx_user_id (user_id),
                INDEX idx_roll_no (roll_no),
                INDEX idx_program_year (program, year),
                INDEX idx_name (first_name, last_name)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
    

    private void createInstructorsTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS instructors (
                instructor_id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT UNIQUE NOT NULL,
                employee_id VARCHAR(20) UNIQUE NOT NULL,
                first_name VARCHAR(100) NOT NULL,
                last_name VARCHAR(100) NOT NULL,
                department VARCHAR(100) NOT NULL,
                designation VARCHAR(50),
                email VARCHAR(100),
                phone VARCHAR(20),
                office_room VARCHAR(50),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                INDEX idx_user_id (user_id),
                INDEX idx_employee_id (employee_id),
                INDEX idx_department (department),
                INDEX idx_name (first_name, last_name)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    /**
     * Create admins table
     */
    private void createAdminsTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS admins (
                admin_id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT UNIQUE NOT NULL,
                admin_code VARCHAR(20) UNIQUE NOT NULL,
                first_name VARCHAR(100) NOT NULL,
                last_name VARCHAR(100) NOT NULL,
                email VARCHAR(100),
                phone VARCHAR(20),
                department VARCHAR(100),
                designation VARCHAR(50),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                INDEX idx_user_id (user_id),
                INDEX idx_admin_code (admin_code),
                INDEX idx_name (first_name, last_name)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    
    private void createCoursesTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS courses (
                course_id INT AUTO_INCREMENT PRIMARY KEY,
                course_code VARCHAR(20) UNIQUE NOT NULL,
                title VARCHAR(200) NOT NULL,
                credits INT NOT NULL,
                description TEXT,
                prerequisite VARCHAR(100),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                INDEX idx_course_code (course_code),
                INDEX idx_credits (credits)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * FIXED: Removed UNIQUE constraint from course_id to allow multiple sections per course
     * FIXED: Changed drop_deadline from DATE to DATETIME to store both date and time
     */
    private void createSectionsTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS sections (
                section_id INT AUTO_INCREMENT PRIMARY KEY,
                course_id INT NOT NULL,
                instructor_id INT,
                section_name VARCHAR(10) NOT NULL,
                day_time VARCHAR(100) NOT NULL,
                room VARCHAR(50),
                capacity INT NOT NULL,
                enrolled_count INT DEFAULT 0,
                semester VARCHAR(20) NOT NULL,
                year INT NOT NULL,
                status ENUM('OPEN', 'CLOSED') DEFAULT 'OPEN',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                drop_deadline DATETIME NULL,
                UNIQUE KEY unique_section (course_id, section_name, semester, year),
                INDEX idx_course_id (course_id),
                INDEX idx_instructor_id (instructor_id),
                INDEX idx_semester_year (semester, year),
                INDEX idx_status (status),
                FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
                FOREIGN KEY (instructor_id) REFERENCES instructors(instructor_id) ON DELETE SET NULL
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    private void createEnrollmentsTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS enrollments (
                enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
                student_id INT NOT NULL,
                section_id INT NOT NULL,
                status ENUM('ACTIVE', 'DROPPED', 'COMPLETED') DEFAULT 'ACTIVE',
                enrollment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                drop_date TIMESTAMP NULL,
                final_grade VARCHAR(5),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                UNIQUE KEY unique_enrollment (student_id, section_id),
                INDEX idx_student_id (student_id),
                INDEX idx_section_id (section_id),
                INDEX idx_status (status),
                FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
                FOREIGN KEY (section_id) REFERENCES sections(section_id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * MODIFIED: New grades table structure with user_id, section_id, quiz, midsem, endsem
     */
    private void createGradesTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS grades (
                grade_id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                section_id INT NOT NULL,
                quiz DECIMAL(5,2) DEFAULT NULL,
                midsem DECIMAL(5,2) DEFAULT NULL,
                endsem DECIMAL(5,2) DEFAULT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                UNIQUE KEY unique_grade (user_id, section_id),
                INDEX idx_user_id (user_id),
                INDEX idx_section_id (section_id)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    private void createSettingsTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS settings (
                setting_key VARCHAR(100) PRIMARY KEY,
                setting_value TEXT NOT NULL,
                description TEXT,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            
            // Initialize maintenance mode setting if not exists
            String insertDefaultSql = """
                INSERT IGNORE INTO settings (setting_key, setting_value, description) 
                VALUES ('maintenance_mode', 'false', 'System maintenance mode flag')
                """;
            stmt.execute(insertDefaultSql);
        }
    }
    
    // ==================== STUDENT OPERATIONS ====================
    
    public int addStudent(int userId, String rollNo, String firstName, String lastName, 
                      String program, int year, String email, String phone, String address) 
                      throws SQLException {
        String sql = "INSERT INTO students (user_id, roll_no, first_name, last_name, program, year, email, phone, address) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, rollNo);
            pstmt.setString(3, firstName);
            pstmt.setString(4, lastName);
            pstmt.setString(5, program);
            pstmt.setInt(6, year);
            pstmt.setString(7, email);
            pstmt.setString(8, phone);
            pstmt.setString(9, address);
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
                throw new SQLException("Creating student failed, no ID obtained.");
            }
        }
    }
    
    public Optional<Student> getStudentByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM students WHERE user_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToStudent(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    public Optional<Student> getStudentById(int studentId) throws SQLException {
        String sql = "SELECT * FROM students WHERE student_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToStudent(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    public List<Student> getAllStudents() throws SQLException {
        String sql = "SELECT * FROM students ORDER BY roll_no";
        List<Student> students = new ArrayList<>();
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
        }
        
        return students;
    }
    
    public void updateStudent(int studentId, String firstName, String lastName,
                                     String program, int year, 
                                     String email, String phone, String address) throws SQLException {
        String sql = "UPDATE students SET first_name = ?, last_name = ?, program = ?, year = ?, email = ?, phone = ?, address = ? WHERE student_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, program);
            pstmt.setInt(4, year);
            pstmt.setString(5, email);
            pstmt.setString(6, phone);
            pstmt.setString(7, address);
            pstmt.setInt(8, studentId);
            pstmt.executeUpdate();
        }
    }
    
    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setStudentId(rs.getInt("student_id"));
        student.setUserId(rs.getInt("user_id"));
        student.setRollNo(rs.getString("roll_no"));
        student.setFirstName(rs.getString("first_name"));
        student.setLastName(rs.getString("last_name"));
        student.setProgram(rs.getString("program"));
        student.setYear(rs.getInt("year"));
        student.setEmail(rs.getString("email"));
        student.setPhone(rs.getString("phone"));
        student.setAddress(rs.getString("address"));
        return student;
    }
    
    // ==================== INSTRUCTOR OPERATIONS ====================
    
    public int addInstructor(int userId, String employeeId, String firstName, String lastName,
                           String department, String designation, String email, 
                           String phone, String officeRoom) throws SQLException {
        String sql = "INSERT INTO instructors (user_id, employee_id, first_name, last_name, department, designation, email, phone, office_room) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, employeeId);
            pstmt.setString(3, firstName);
            pstmt.setString(4, lastName);
            pstmt.setString(5, department);
            pstmt.setString(6, designation);
            pstmt.setString(7, email);
            pstmt.setString(8, phone);
            pstmt.setString(9, officeRoom);
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
                throw new SQLException("Creating instructor failed, no ID obtained.");
            }
        }
    }
    
    public Optional<Instructor> getInstructorByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM instructors WHERE user_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToInstructor(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    public Optional<Instructor> getInstructorById(int instructorId) throws SQLException {
        String sql = "SELECT * FROM instructors WHERE instructor_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, instructorId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToInstructor(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    public List<Instructor> getAllInstructors() throws SQLException {
        String sql = "SELECT * FROM instructors ORDER BY employee_id";
        List<Instructor> instructors = new ArrayList<>();
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                instructors.add(mapResultSetToInstructor(rs));
            }
        }
        
        return instructors;
    }

    public void updateInstructor(int instructorId, String firstName, String lastName,
                                        String department, String designation, 
                                        String email, String phone, String officeRoom) throws SQLException {
        String sql = "UPDATE instructors SET first_name = ?, last_name = ?, department = ?, designation = ?, email = ?, phone = ?, office_room = ? WHERE instructor_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, department);
            pstmt.setString(4, designation);
            pstmt.setString(5, email);
            pstmt.setString(6, phone);
            pstmt.setString(7, officeRoom);
            pstmt.setInt(8, instructorId);
            pstmt.executeUpdate();
        }
    }
    
    private Instructor mapResultSetToInstructor(ResultSet rs) throws SQLException {
        Instructor instructor = new Instructor();
        instructor.setInstructorId(rs.getInt("instructor_id"));
        instructor.setUserId(rs.getInt("user_id"));
        instructor.setEmployeeId(rs.getString("employee_id"));
        instructor.setFirstName(rs.getString("first_name"));
        instructor.setLastName(rs.getString("last_name"));
        instructor.setDepartment(rs.getString("department"));
        instructor.setDesignation(rs.getString("designation"));
        instructor.setEmail(rs.getString("email"));
        instructor.setPhone(rs.getString("phone"));
        instructor.setOfficeRoom(rs.getString("office_room"));
        return instructor;
    }

    // ==================== ADMIN OPERATIONS ====================
    
    public int addAdmin(int userId, String adminCode, String firstName, String lastName,
                       String email, String phone, String department, String designation) 
                       throws SQLException {
        String sql = "INSERT INTO admins (user_id, admin_code, first_name, last_name, email, phone, department, designation) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, adminCode);
            pstmt.setString(3, firstName);
            pstmt.setString(4, lastName);
            pstmt.setString(5, email);
            pstmt.setString(6, phone);
            pstmt.setString(7, department);
            pstmt.setString(8, designation);
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
                throw new SQLException("Creating admin failed, no ID obtained.");
            }
        }
    }
    
    public Optional<Admin> getAdminByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM admins WHERE user_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAdmin(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    public Optional<Admin> getAdminById(int adminId) throws SQLException {
        String sql = "SELECT * FROM admins WHERE admin_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, adminId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAdmin(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    public List<Admin> getAllAdmins() throws SQLException {
        String sql = "SELECT * FROM admins ORDER BY admin_code";
        List<Admin> admins = new ArrayList<>();
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                admins.add(mapResultSetToAdmin(rs));
            }
        }
        
        return admins;
    }
    
    public void updateAdmin(int adminId, String firstName, String lastName,
                                   String email, String phone, 
                                   String department, String designation) throws SQLException {
        String sql = "UPDATE admins SET first_name = ?, last_name = ?, email = ?, phone = ?, department = ?, designation = ? WHERE admin_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, email);
            pstmt.setString(4, phone);
            pstmt.setString(5, department);
            pstmt.setString(6, designation);
            pstmt.setInt(7, adminId);
            pstmt.executeUpdate();
        }
    }
    
    private Admin mapResultSetToAdmin(ResultSet rs) throws SQLException {
        Admin admin = new Admin();
        admin.setAdminId(rs.getInt("admin_id"));
        admin.setUserId(rs.getInt("user_id"));
        admin.setAdminCode(rs.getString("admin_code"));
        admin.setFirstName(rs.getString("first_name"));
        admin.setLastName(rs.getString("last_name"));
        admin.setEmail(rs.getString("email"));
        admin.setPhone(rs.getString("phone"));
        admin.setDepartment(rs.getString("department"));
        admin.setDesignation(rs.getString("designation"));
        return admin;
    }

    // ==================== COURSE OPERATIONS ====================
    
    public int addCourse(String courseCode, String title, int credits, String description, String prerequisite) throws SQLException {
        String sql = "INSERT INTO courses (course_code, title, credits, description, prerequisite) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, courseCode);
            pstmt.setString(2, title);
            pstmt.setInt(3, credits);
            pstmt.setString(4, description);
            pstmt.setString(5, prerequisite);
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
                throw new SQLException("Creating course failed, no ID obtained.");
            }
        }
    }
    
    public Optional<Course> getCourseById(int courseId) throws SQLException {
        String sql = "SELECT * FROM courses WHERE course_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCourse(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    public Optional<Course> getCourseByCode(String courseCode) throws SQLException {
        String sql = "SELECT * FROM courses WHERE course_code = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, courseCode);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCourse(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    public List<Course> getAllCourses() throws SQLException {
        String sql = "SELECT * FROM courses ORDER BY course_code";
        List<Course> courses = new ArrayList<>();
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                courses.add(mapResultSetToCourse(rs));
            }
        }
        
        return courses;
    }
    
    public void updateCourse(int courseId, String title, int credits, String description, String prerequisite) throws SQLException {
        String sql = "UPDATE courses SET title = ?, credits = ?, description = ?, prerequisite = ? WHERE course_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setInt(2, credits);
            pstmt.setString(3, description);
            pstmt.setString(4, prerequisite);
            pstmt.setInt(5, courseId);
            pstmt.executeUpdate();
        }
    }
    
    private Course mapResultSetToCourse(ResultSet rs) throws SQLException {
        Course course = new Course();
        course.setCourseId(rs.getInt("course_id"));
        course.setCourseCode(rs.getString("course_code"));
        course.setTitle(rs.getString("title"));
        course.setCredits(rs.getInt("credits"));
        course.setDescription(rs.getString("description"));
        course.setPrerequisite(rs.getString("prerequisite"));
        return course;
    }
    
    // ==================== SECTION OPERATIONS ====================
    
    /**
     * EXISTING METHOD - Keep for backward compatibility
     */
    public int addSection(int courseId, Integer instructorId, String sectionName, String dayTime, 
                        String room, int capacity, String semester, int year) throws SQLException {
        return addSectionWithDeadline(courseId, instructorId, sectionName, dayTime, 
                                    room, capacity, semester, year, null);
    }

    /**
     * NEW: Add section with drop deadline support (DATETIME format)
     * If dropDeadline is null, no deadline is set (students can always drop)
     * @param dropDeadline LocalDateTime with both date and time information
     */
    public int addSectionWithDeadline(int courseId, Integer instructorId, String sectionName, String dayTime, 
                                    String room, int capacity, String semester, int year, 
                                    java.time.LocalDateTime dropDeadline) throws SQLException {
        String sql = "INSERT INTO sections (course_id, instructor_id, section_name, day_time, room, " +
                    "capacity, semester, year, status, drop_deadline) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'OPEN', ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, courseId);
            if (instructorId != null) {
                pstmt.setInt(2, instructorId);
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            pstmt.setString(3, sectionName);
            pstmt.setString(4, dayTime);
            pstmt.setString(5, room);
            pstmt.setInt(6, capacity);
            pstmt.setString(7, semester);
            pstmt.setInt(8, year);
            
            // Set drop deadline (can be null) - Now using DATETIME
            if (dropDeadline != null) {
                pstmt.setTimestamp(9, java.sql.Timestamp.valueOf(dropDeadline));
            } else {
                pstmt.setNull(9, Types.TIMESTAMP);
            }
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
                throw new SQLException("Creating section failed, no ID obtained.");
            }
        }
    }

    
    public void updateSectionStatus(int sectionId, String status) throws SQLException {
        String sql = "UPDATE sections SET status = ? WHERE section_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, sectionId);
            pstmt.executeUpdate();
        }
    }

    public Optional<Section> getSectionById(int sectionId) throws SQLException {
        String sql = """
            SELECT s.*, c.course_code, c.title as course_title, c.credits, c.description,
                   i.employee_id, i.department, 
                   CONCAT(COALESCE(i.employee_id, ''), ' - ', COALESCE(i.department, 'TBA')) as instructor_name
            FROM sections s
            JOIN courses c ON s.course_id = c.course_id
            LEFT JOIN instructors i ON s.instructor_id = i.instructor_id
            WHERE s.section_id = ?
            """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, sectionId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSection(rs));
                }
            }
        }
        
        return Optional.empty();
    }

    /**
     * Get ALL enrollments for a section (including ACTIVE, DROPPED, and COMPLETED)
     */
    public List<Enrollment> getSectionEnrollmentsAll(int sectionId) throws SQLException {
        String sql = """
            SELECT e.*, s.roll_no, s.program, s.year as student_year
            FROM enrollments e
            JOIN students s ON e.student_id = s.student_id
            WHERE e.section_id = ?
            ORDER BY e.status DESC, s.roll_no
            """;
        
        List<Enrollment> enrollments = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, sectionId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Enrollment enrollment = new Enrollment();
                    enrollment.setEnrollmentId(rs.getInt("enrollment_id"));
                    enrollment.setStudentId(rs.getInt("student_id"));
                    enrollment.setSectionId(rs.getInt("section_id"));
                    enrollment.setStatus(rs.getString("status"));
                    enrollment.setFinalGrade(rs.getString("final_grade"));
                    
                    // Additional student info
                    enrollment.setStudentRollNo(rs.getString("roll_no"));
                    enrollment.setStudentProgram(rs.getString("program"));
                    enrollment.setStudentYear(rs.getInt("student_year"));
                    
                    enrollments.add(enrollment);
                }
            }
        }
        
        return enrollments;
    }
    
    public List<Section> getSectionsBySemesterYear(String semester, int year) throws SQLException {
        String sql = """
            SELECT s.*, c.course_code, c.title as course_title, c.credits, c.description,
                   i.employee_id, i.department, i.first_name, i.last_name,
                   CONCAT(COALESCE(i.first_name, ''), ' ', COALESCE(i.last_name, '')) as instructor_name
            FROM sections s
            JOIN courses c ON s.course_id = c.course_id
            LEFT JOIN instructors i ON s.instructor_id = i.instructor_id
            WHERE s.semester = ? AND s.year = ?
            ORDER BY c.course_code, s.section_name
            """;
        
        List<Section> sections = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, semester);
            pstmt.setInt(2, year);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    sections.add(mapResultSetToSection(rs));
                }
            }
        }
        
        return sections;
    }
    
    public List<Section> getSectionsByInstructor(int instructorId, String semester, int year) throws SQLException {
        String sql = """
            SELECT s.*, c.course_code, c.title as course_title, c.credits, c.description,
                   i.employee_id, i.department, i.first_name, i.last_name,
                   CONCAT(i.first_name, ' ', i.last_name) as instructor_name
            FROM sections s
            JOIN courses c ON s.course_id = c.course_id
            JOIN instructors i ON s.instructor_id = i.instructor_id
            WHERE s.instructor_id = ? AND s.semester = ? AND s.year = ?
            ORDER BY c.course_code, s.section_name
            """;
        
        List<Section> sections = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, instructorId);
            pstmt.setString(2, semester);
            pstmt.setInt(3, year);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    sections.add(mapResultSetToSection(rs));
                }
            }
        }
        
        return sections;
    }

    public List<Section> getAllSections() throws SQLException {
        String sql = """
            SELECT s.*, c.course_code, c.title as course_title, c.credits, c.description,
                i.employee_id, i.department, i.first_name, i.last_name,
                CONCAT(COALESCE(i.first_name, ''), ' ', COALESCE(i.last_name, '')) as instructor_name
            FROM sections s
            JOIN courses c ON s.course_id = c.course_id
            LEFT JOIN instructors i ON s.instructor_id = i.instructor_id
            ORDER BY s.year DESC, s.semester, c.course_code, s.section_name
            """;
        
        List<Section> sections = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    sections.add(mapResultSetToSection(rs));
                }
            }
        }
        
        return sections;
    }
    
    public void assignInstructorToSection(int sectionId, int instructorId) throws SQLException {
        String sql = "UPDATE sections SET instructor_id = ? WHERE section_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, instructorId);
            pstmt.setInt(2, sectionId);
            pstmt.executeUpdate();
        }
    }
    
    public void updateSectionEnrollmentCount(int sectionId) throws SQLException {
        String sql = """
            UPDATE sections s
            SET enrolled_count = (
                SELECT COUNT(*) FROM enrollments 
                WHERE section_id = ? AND status = 'ACTIVE'
            )
            WHERE section_id = ?
            """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, sectionId);
            pstmt.setInt(2, sectionId);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * UPDATED: Map ResultSet to Section - Now includes drop_deadline as DATETIME
     */
    private Section mapResultSetToSection(ResultSet rs) throws SQLException {
        Section section = new Section();
        section.setSectionId(rs.getInt("section_id"));
        section.setCourseId(rs.getInt("course_id"));
        
        Integer instructorId = rs.getInt("instructor_id");
        section.setInstructorId(rs.wasNull() ? null : instructorId);
        
        section.setSectionName(rs.getString("section_name"));
        section.setDayTime(rs.getString("day_time"));
        section.setRoom(rs.getString("room"));
        section.setCapacity(rs.getInt("capacity"));
        section.setEnrolledCount(rs.getInt("enrolled_count"));
        section.setSemester(rs.getString("semester"));
        section.setYear(rs.getInt("year"));
        section.setStatus(rs.getString("status"));
        
        // UPDATED: Add drop deadline mapping - Now DATETIME instead of DATE
        java.sql.Timestamp dropDeadlineTimestamp = rs.getTimestamp("drop_deadline");
        if (dropDeadlineTimestamp != null) {
            section.setDropDeadline(dropDeadlineTimestamp.toLocalDateTime());
        }
        
        // Additional fields from JOINs (if present)
        try {
            section.setCourseCode(rs.getString("course_code"));
            section.setCourseTitle(rs.getString("course_title"));
            section.setCredits(rs.getInt("credits"));
            section.setDescription(rs.getString("description"));
            section.setInstructorName(rs.getString("instructor_name"));
        } catch (SQLException e) {
            // These columns might not be present in all queries
        }
        
        return section;
    }
    
    // ==================== ENROLLMENT OPERATIONS ====================
    
    public int enrollStudent(int studentId, int sectionId) throws SQLException {
        // Check if there's an existing enrollment (any status)
        String checkSql = "SELECT enrollment_id, status FROM enrollments WHERE student_id = ? AND section_id = ?";
        
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setInt(1, studentId);
            checkStmt.setInt(2, sectionId);
            
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    int enrollmentId = rs.getInt("enrollment_id");
                    String status = rs.getString("status");
                    
                    if ("ACTIVE".equals(status)) {
                        return enrollmentId;
                    } else if ("DROPPED".equals(status)) {
                        // Reactivate the dropped enrollment
                        String reactivateSql = "UPDATE enrollments SET status = 'ACTIVE', enrollment_date = CURRENT_TIMESTAMP, drop_date = NULL, final_grade = NULL WHERE enrollment_id = ?";
                        try (PreparedStatement reactivateStmt = connection.prepareStatement(reactivateSql)) {
                            reactivateStmt.setInt(1, enrollmentId);
                            reactivateStmt.executeUpdate();
                        }
                        
                        // Update section enrollment count
                        updateSectionEnrollmentCount(sectionId);
                        
                        return enrollmentId;
                    }
                }
            }
        }
        
        // No existing enrollment, create new one
        String insertSql = "INSERT INTO enrollments (student_id, section_id, status) VALUES (?, ?, ?)";
        
        try (PreparedStatement insertStmt = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            insertStmt.setInt(1, studentId);
            insertStmt.setInt(2, sectionId);
            insertStmt.setString(3, ENROLLMENT_ACTIVE);
            insertStmt.executeUpdate();
            
            try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int enrollmentId = generatedKeys.getInt(1);
                    
                    // Update section enrollment count
                    updateSectionEnrollmentCount(sectionId);
                    
                    return enrollmentId;
                }
                throw new SQLException("Creating enrollment failed, no ID obtained.");
            }
        }
    }
    
    public void dropEnrollment(int enrollmentId) throws SQLException {
        String sql = "UPDATE enrollments SET status = ?, drop_date = CURRENT_TIMESTAMP WHERE enrollment_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, ENROLLMENT_DROPPED);
            pstmt.setInt(2, enrollmentId);
            pstmt.executeUpdate();
            
            // Get section ID to update enrollment count
            String getSectionSql = "SELECT section_id FROM enrollments WHERE enrollment_id = ?";
            try (PreparedStatement getSectionStmt = connection.prepareStatement(getSectionSql)) {
                getSectionStmt.setInt(1, enrollmentId);
                try (ResultSet rs = getSectionStmt.executeQuery()) {
                    if (rs.next()) {
                        updateSectionEnrollmentCount(rs.getInt("section_id"));
                    }
                }
            }
        }
    }
    
    public List<Enrollment> getStudentEnrollments(int studentId) throws SQLException {
        String sql = """
            SELECT e.*, 
                   s.course_id, s.section_name, s.semester, s.year, s.day_time, s.room,
                   c.course_code, c.title as course_title, c.credits, c.description,
                   i.employee_id, i.department, i.first_name, i.last_name,
                   CONCAT(COALESCE(i.first_name, ''), ' ', COALESCE(i.last_name, '')) as instructor_name
            FROM enrollments e
            JOIN sections s ON e.section_id = s.section_id
            JOIN courses c ON s.course_id = c.course_id
            LEFT JOIN instructors i ON s.instructor_id = i.instructor_id
            WHERE e.student_id = ?
            ORDER BY s.year DESC, s.semester, c.course_code
            """;
        
        List<Enrollment> enrollments = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Enrollment enrollment = new Enrollment();
                    enrollment.setEnrollmentId(rs.getInt("enrollment_id"));
                    enrollment.setStudentId(rs.getInt("student_id"));
                    enrollment.setSectionId(rs.getInt("section_id"));
                    enrollment.setStatus(rs.getString("status"));
                    enrollment.setFinalGrade(rs.getString("final_grade"));
                    
                    // Section and course info
                    enrollment.setCourseCode(rs.getString("course_code"));
                    enrollment.setCourseTitle(rs.getString("course_title"));
                    enrollment.setSectionName(rs.getString("section_name"));
                    enrollment.setSemester(rs.getString("semester"));
                    enrollment.setYear(rs.getInt("year"));
                    enrollment.setCredits(rs.getInt("credits"));
                    enrollment.setInstructorName(rs.getString("instructor_name"));
                    enrollment.setDayTime(rs.getString("day_time"));
                    enrollment.setRoom(rs.getString("room"));
                    
                    enrollments.add(enrollment);
                }
            }
        }
        
        return enrollments;
    }
    
    public Optional<Enrollment> getEnrollmentById(int enrollmentId) throws SQLException {
        String sql = "SELECT * FROM enrollments WHERE enrollment_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, enrollmentId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Enrollment enrollment = new Enrollment();
                    enrollment.setEnrollmentId(rs.getInt("enrollment_id"));
                    enrollment.setStudentId(rs.getInt("student_id"));
                    enrollment.setSectionId(rs.getInt("section_id"));
                    enrollment.setStatus(rs.getString("status"));
                    enrollment.setFinalGrade(rs.getString("final_grade"));
                    return Optional.of(enrollment);
                }
            }
        }
        
        return Optional.empty();
    }
    
    public void updateEnrollmentGrade(int enrollmentId, String finalGrade) throws SQLException {
        String sql = "UPDATE enrollments SET final_grade = ? WHERE enrollment_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, finalGrade);
            pstmt.setInt(2, enrollmentId);
            pstmt.executeUpdate();
        }
    }
    
    // ==================== GRADE OPERATIONS (MODIFIED) ====================
    
    /**
     * MODIFIED: Get grade by user_id and section_id
     */
    public Optional<Grade> getGradeByUserSection(int userId, int sectionId) throws SQLException {
        String sql = "SELECT * FROM grades WHERE user_id = ? AND section_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, sectionId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToGrade(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * MODIFIED: Update or insert grade components
     */
    public void upsertGrade(int userId, int sectionId, Double quiz, Double midsem, Double endsem) 
            throws SQLException {
        String sql = """
            INSERT INTO grades (user_id, section_id, quiz, midsem, endsem) 
            VALUES (?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE 
                quiz = COALESCE(VALUES(quiz), quiz),
                midsem = COALESCE(VALUES(midsem), midsem),
                endsem = COALESCE(VALUES(endsem), endsem)
            """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, sectionId);
            
            if (quiz != null) {
                pstmt.setDouble(3, quiz);
            } else {
                pstmt.setNull(3, Types.DECIMAL);
            }
            
            if (midsem != null) {
                pstmt.setDouble(4, midsem);
            } else {
                pstmt.setNull(4, Types.DECIMAL);
            }
            
            if (endsem != null) {
                pstmt.setDouble(5, endsem);
            } else {
                pstmt.setNull(5, Types.DECIMAL);
            }
            
            pstmt.executeUpdate();
        }
    }
    
    /**
     * MODIFIED: Get all grades for a section
     */
    public List<Grade> getGradesBySection(int sectionId) throws SQLException {
        String sql = """
            SELECT g.*, s.roll_no, s.first_name, s.last_name
            FROM grades g
            JOIN students s ON g.user_id = s.user_id
            WHERE g.section_id = ?
            ORDER BY s.roll_no
            """;
        
        List<Grade> grades = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, sectionId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Grade grade = mapResultSetToGrade(rs);
                    // Add student info
                    grade.setStudentRollNo(rs.getString("roll_no"));
                    grade.setStudentName(rs.getString("first_name") + " " + rs.getString("last_name"));
                    grades.add(grade);
                }
            }
        }
        
        return grades;
    }
    
    /**
     * MODIFIED: Get all grades for a student (by user_id)
     */
    public List<Grade> getGradesByUser(int userId) throws SQLException {
        String sql = """
            SELECT g.*, 
                   c.course_code, c.title as course_title, c.credits,
                   sec.section_name, sec.semester, sec.year
            FROM grades g
            JOIN sections sec ON g.section_id = sec.section_id
            JOIN courses c ON sec.course_id = c.course_id
            WHERE g.user_id = ?
            ORDER BY sec.year DESC, sec.semester, c.course_code
            """;
        
        List<Grade> grades = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Grade grade = mapResultSetToGrade(rs);
                    // Add course/section info
                    grade.setCourseCode(rs.getString("course_code"));
                    grade.setCourseTitle(rs.getString("course_title"));
                    grade.setSectionName(rs.getString("section_name"));
                    grade.setSemester(rs.getString("semester"));
                    grade.setYear(rs.getInt("year"));
                    grade.setCredits(rs.getInt("credits"));
                    grades.add(grade);
                }
            }
        }
        
        return grades;
    }
    
    /**
     * MODIFIED: Map ResultSet to Grade object
     */
    private Grade mapResultSetToGrade(ResultSet rs) throws SQLException {
        Grade grade = new Grade();
        grade.setGradeId(rs.getInt("grade_id"));
        grade.setUserId(rs.getInt("user_id"));
        grade.setSectionId(rs.getInt("section_id"));
        
        // Get nullable decimal values
        Double quiz = rs.getDouble("quiz");
        grade.setQuiz(rs.wasNull() ? null : quiz);
        
        Double midsem = rs.getDouble("midsem");
        grade.setMidsem(rs.wasNull() ? null : midsem);
        
        Double endsem = rs.getDouble("endsem");
        grade.setEndsem(rs.wasNull() ? null : endsem);
        
        return grade;
    }
    
    // ==================== SETTINGS OPERATIONS ====================
    
    public String getSetting(String key) throws SQLException {
        String sql = "SELECT setting_value FROM settings WHERE setting_key = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, key);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("setting_value");
                }
            }
        }
        
        return null;
    }
    
    public void updateSetting(String key, String value) throws SQLException {
        String sql = """
            INSERT INTO settings (setting_key, setting_value) 
            VALUES (?, ?)
            ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value)
            """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, key);
            pstmt.setString(2, value);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Set a setting with description
     */
    public void setSetting(String key, String value, String description) throws SQLException {
        String sql = """
            INSERT INTO settings (setting_key, setting_value, description) 
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value), description = VALUES(description)
            """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, key);
            pstmt.setString(2, value);
            pstmt.setString(3, description);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Check if system is in maintenance mode
     */
    public boolean isMaintenanceMode() throws SQLException {
        String value = getSetting("maintenance_mode");
        return value != null && value.equalsIgnoreCase("true");
    }
    
    /**
     * Set maintenance mode
     * @param enabled true to enable maintenance mode, false to disable
     */
    public void setMaintenanceMode(boolean enabled) throws SQLException {
        updateSetting("maintenance_mode", enabled ? "true" : "false");
    }
    
    // ==================== ADDITIONAL ENROLLMENT/GRADE METHODS ====================
    
    /**
     * Check if a student is enrolled in a specific section (active enrollment only)
     */
    public boolean isStudentEnrolled(int studentId, int sectionId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND section_id = ? AND status = 'ACTIVE'";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, sectionId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Get student enrollments with optional status filter
     * @param studentId Student ID
     * @param status Status filter (ACTIVE, DROPPED, COMPLETED) or null for all
     */
    public List<Enrollment> getStudentEnrollments(int studentId, String status) throws SQLException {
        String sql;
        if (status != null) {
            sql = """
                SELECT e.*, 
                       s.course_id, s.section_name, s.semester, s.year, s.day_time, s.room,
                       c.course_code, c.title as course_title, c.credits, c.description,
                       i.employee_id, i.department, i.first_name, i.last_name,
                       CONCAT(COALESCE(i.first_name, ''), ' ', COALESCE(i.last_name, '')) as instructor_name
                FROM enrollments e
                JOIN sections s ON e.section_id = s.section_id
                JOIN courses c ON s.course_id = c.course_id
                LEFT JOIN instructors i ON s.instructor_id = i.instructor_id
                WHERE e.student_id = ? AND e.status = ?
                ORDER BY s.year DESC, s.semester, c.course_code
                """;
        } else {
            sql = """
                SELECT e.*, 
                       s.course_id, s.section_name, s.semester, s.year, s.day_time, s.room,
                       c.course_code, c.title as course_title, c.credits, c.description,
                       i.employee_id, i.department, i.first_name, i.last_name,
                       CONCAT(COALESCE(i.first_name, ''), ' ', COALESCE(i.last_name, '')) as instructor_name
                FROM enrollments e
                JOIN sections s ON e.section_id = s.section_id
                JOIN courses c ON s.course_id = c.course_id
                LEFT JOIN instructors i ON s.instructor_id = i.instructor_id
                WHERE e.student_id = ?
                ORDER BY s.year DESC, s.semester, c.course_code
                """;
        }
        
        List<Enrollment> enrollments = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            if (status != null) {
                pstmt.setString(2, status);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Enrollment enrollment = new Enrollment();
                    enrollment.setEnrollmentId(rs.getInt("enrollment_id"));
                    enrollment.setStudentId(rs.getInt("student_id"));
                    enrollment.setSectionId(rs.getInt("section_id"));
                    enrollment.setStatus(rs.getString("status"));
                    enrollment.setFinalGrade(rs.getString("final_grade"));
                    
                    // Section and course info
                    enrollment.setCourseCode(rs.getString("course_code"));
                    enrollment.setCourseTitle(rs.getString("course_title"));
                    enrollment.setSectionName(rs.getString("section_name"));
                    enrollment.setSemester(rs.getString("semester"));
                    enrollment.setYear(rs.getInt("year"));
                    enrollment.setCredits(rs.getInt("credits"));
                    enrollment.setInstructorName(rs.getString("instructor_name"));
                    enrollment.setDayTime(rs.getString("day_time"));
                    enrollment.setRoom(rs.getString("room"));
                    
                    enrollments.add(enrollment);
                }
            }
        }
        
        return enrollments;
    }
    
    /**
     * Get enrollments for a section (active enrollments only)
     */
    public List<Enrollment> getSectionEnrollments(int sectionId) throws SQLException {
        String sql = """
            SELECT e.*, s.roll_no, s.first_name, s.last_name, s.program, s.year as student_year
            FROM enrollments e
            JOIN students s ON e.student_id = s.student_id
            WHERE e.section_id = ? AND e.status = 'ACTIVE'
            ORDER BY s.roll_no
            """;
        
        List<Enrollment> enrollments = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, sectionId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Enrollment enrollment = new Enrollment();
                    enrollment.setEnrollmentId(rs.getInt("enrollment_id"));
                    enrollment.setStudentId(rs.getInt("student_id"));
                    enrollment.setSectionId(rs.getInt("section_id"));
                    enrollment.setStatus(rs.getString("status"));
                    enrollment.setFinalGrade(rs.getString("final_grade"));
                    
                    // Additional student info
                    enrollment.setStudentRollNo(rs.getString("roll_no"));
                    enrollment.setStudentName(rs.getString("first_name") + " " + rs.getString("last_name"));
                    enrollment.setStudentProgram(rs.getString("program"));
                    enrollment.setStudentYear(rs.getInt("student_year"));
                    
                    enrollments.add(enrollment);
                }
            }
        }
        
        return enrollments;
    }
    
    /**
     * Save or update grade for a student in a section
     */
    public void saveOrUpdateGrade(int userId, int sectionId, Double quiz, Double midsem, Double endsem) 
            throws SQLException {
        String sql = """
            INSERT INTO grades (user_id, section_id, quiz, midsem, endsem) 
            VALUES (?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE 
                quiz = COALESCE(VALUES(quiz), quiz),
                midsem = COALESCE(VALUES(midsem), midsem),
                endsem = COALESCE(VALUES(endsem), endsem)
            """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, sectionId);
            
            if (quiz != null) {
                pstmt.setDouble(3, quiz);
            } else {
                pstmt.setNull(3, Types.DECIMAL);
            }
            
            if (midsem != null) {
                pstmt.setDouble(4, midsem);
            } else {
                pstmt.setNull(4, Types.DECIMAL);
            }
            
            if (endsem != null) {
                pstmt.setDouble(5, endsem);
            } else {
                pstmt.setNull(5, Types.DECIMAL);
            }
            
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Get grade by user ID and section ID
     */
    public Optional<Grade> getGradeByUserAndSection(int userId, int sectionId) throws SQLException {
        String sql = """
            SELECT g.*, 
                   c.course_code, c.title as course_title, c.credits,
                   sec.section_name, sec.semester, sec.year
            FROM grades g
            JOIN sections sec ON g.section_id = sec.section_id
            JOIN courses c ON sec.course_id = c.course_id
            WHERE g.user_id = ? AND g.section_id = ?
            """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, sectionId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Grade grade = mapResultSetToGrade(rs);
                    // Add course/section info
                    grade.setCourseCode(rs.getString("course_code"));
                    grade.setCourseTitle(rs.getString("course_title"));
                    grade.setSectionName(rs.getString("section_name"));
                    grade.setSemester(rs.getString("semester"));
                    grade.setYear(rs.getInt("year"));
                    grade.setCredits(rs.getInt("credits"));
                    return Optional.of(grade);
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Update final grade in enrollment
     */
    public void updateFinalGrade(int enrollmentId, String letterGrade) throws SQLException {
        String sql = "UPDATE enrollments SET final_grade = ? WHERE enrollment_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, letterGrade);
            pstmt.setInt(2, enrollmentId);
            pstmt.executeUpdate();
        }
    }
}
