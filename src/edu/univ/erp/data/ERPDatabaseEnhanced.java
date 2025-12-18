package edu.univ.erp.data;

import java.sql.*;

/**
 * Enhanced ERP Database Schema with Name Fields and Sample Data
 * This file contains the MODIFIED table creation methods to add:
 * - first_name and last_name fields to students table
 * - first_name and last_name fields to instructors table
 * - Complete sample student data with grades
 */
public class ERPDatabaseEnhanced {
    
    /**
     * REPLACE the createStudentsTable() method in ERPDatabase.java with this:
     */
    public static String getEnhancedStudentsTableSQL() {
        return """
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
    }
    
    /**
     * REPLACE the createInstructorsTable() method in ERPDatabase.java with this:
     */
    public static String getEnhancedInstructorsTableSQL() {
        return """
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
    }
    
    /**
     * ADD this method to ERPDatabase.java - Enhanced addStudent with name fields
     */
    public static String getAddStudentWithNameSQL() {
        return "INSERT INTO students (user_id, roll_no, first_name, last_name, program, year, email, phone, address) " +
               "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }
    
    /**
     * ADD this method to ERPDatabase.java - Enhanced addInstructor with name fields
     */
    public static String getAddInstructorWithNameSQL() {
        return "INSERT INTO instructors (user_id, employee_id, first_name, last_name, department, designation, email, phone, office_room) " +
               "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }
    
    /**
     * Complete sample data seeding SQL
     * This creates a complete sample student with full academic history
     */
    public static String getSampleDataSQL() {
        return """
            -- Sample Data for Complete Student Profile
            
            -- 1. Sample Admin (already exists in most setups)
            -- Username: admin, Password: admin123
            
            -- 2. Sample Instructor 1
            -- Username: dr.sharma, Password: instructor123
            -- User ID will be auto-generated
            
            -- 3. Sample Instructor 2
            -- Username: prof.kumar, Password: instructor123
            
            -- 4. Sample Student 1 (Complete Profile)
            -- Username: stu001, Password: student123
            
            -- Note: These are example SQL statements. Actual insertion must be done
            -- through the application with proper password hashing.
            
            -- Sample Courses for different semesters
            -- Monsoon 2023 courses
            INSERT IGNORE INTO courses (course_code, title, credits, description) VALUES
            ('CS101', 'Introduction to Programming', 4, 'Fundamentals of programming using Python'),
            ('MATH101', 'Calculus I', 4, 'Differential and integral calculus'),
            ('PHY101', 'Physics I', 4, 'Mechanics and thermodynamics'),
            ('HUM101', 'Technical Communication', 3, 'Writing and presentation skills');
            
            -- Winter 2024 courses
            INSERT IGNORE INTO courses (course_code, title, credits, description) VALUES
            ('CS201', 'Data Structures', 4, 'Arrays, linked lists, trees, graphs'),
            ('CS202', 'Computer Architecture', 4, 'Digital logic and computer organization'),
            ('MATH201', 'Discrete Mathematics', 4, 'Logic, sets, relations, graphs'),
            ('HUM201', 'Professional Ethics', 2, 'Ethics in technology and society');
            
            -- Monsoon 2024 courses
            INSERT IGNORE INTO courses (course_code, title, credits, description) VALUES
            ('CS301', 'Database Systems', 4, 'Relational databases and SQL'),
            ('CS302', 'Operating Systems', 4, 'Process management and memory'),
            ('CS303', 'Algorithms', 4, 'Algorithm design and analysis'),
            ('MGMT301', 'Project Management', 3, 'Software project planning');
            
            -- This is just the schema enhancement documentation.
            -- Actual data will be inserted through Java application with:
            -- 1. Proper BCrypt password hashing
            -- 2. Foreign key relationships
            -- 3. Enrollment records with timestamps
            -- 4. Grade entries for each enrollment
            """;
    }
}
