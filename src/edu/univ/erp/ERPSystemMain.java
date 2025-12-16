package edu.univ.erp;

import edu.univ.erp.data.SeedDataWithNames;
import edu.univ.erp.data.AuthDatabase;
import edu.univ.erp.data.ERPDatabase;
import edu.univ.erp.service.AuthService;
import edu.univ.erp.service.AccessControlService;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.service.CatalogService;
import edu.univ.erp.service.GradeComputationService;
import edu.univ.erp.ui.auth.LoginFrame;

import javax.swing.*;
import java.sql.*;

public class ERPSystemMain {

    // ===== DATABASE CONFIG =====
    private static final String HOST = "jdbc:mysql://localhost:3306/";
    private static final String AUTH_DB = "auth_db";
    private static final String ERP_DB = "erp_db";

    private static final String AUTH_URL = HOST + AUTH_DB + "?allowMultiQueries=true&serverTimezone=UTC";
    private static final String ERP_URL  = HOST + ERP_DB  + "?allowMultiQueries=true&serverTimezone=UTC";

    private static final String USER = "root";
    private static final String PASS = "Neha@0107";

    // ===== SHARED OBJECTS =====
    private static Connection authConn;
    private static Connection erpConn;

    private static AuthDatabase authDb;
    private static ERPDatabase erpDb;

    private static AuthService authService;
    private static AdminService adminService;
    private static StudentService studentService;
    private static InstructorService instructorService;
    private static CatalogService catalogService;

    // helper services
    private static AccessControlService accessControlService;
    private static GradeComputationService gradeComputationService;


    // =======================================================
    //                     MAIN METHOD
    // =======================================================
    public static void main(String[] args) {

        System.out.println("🚀 Starting ERP System...");

        try {
            loadDriver();

            connectDatabases();

            // Add shutdown hook to close connections cleanly
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("-> Shutdown hook running: closing DB connections...");
                closeConnections();
            }));

            initDatabases();
            insertInitialData();
            initServices();
            launchUI();

            System.out.println("🎉 ERP System started successfully.");

        } catch (Exception e) {
            System.err.println("❌ Fatal Startup Error: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getMessage(), "Startup Error", JOptionPane.ERROR_MESSAGE);
            // Ensure connections are closed in case of fatal error
            closeConnections();
        }
    }


    // =======================================================
    //                STEP 1: Load MySQL Driver
    // =======================================================
    private static void loadDriver() throws ClassNotFoundException {
        System.out.println("-> Loading MySQL JDBC Driver...");
        Class.forName("com.mysql.cj.jdbc.Driver");
        System.out.println("   MySQL Driver loaded.");
    }


    // =======================================================
    //                STEP 2: Connect to DBs
    // =======================================================
    private static void connectDatabases() throws SQLException {
        System.out.println("\n-> Connecting to AUTH DB...");
        authConn = DriverManager.getConnection(AUTH_URL, USER, PASS);
        System.out.println("   Connected to " + AUTH_DB);

        System.out.println("-> Connecting to ERP DB...");
        erpConn = DriverManager.getConnection(ERP_URL, USER, PASS);
        System.out.println("   Connected to " + ERP_DB);

        authDb = new AuthDatabase(authConn);
        erpDb  = new ERPDatabase(erpConn);
    }


    // =======================================================
    //     STEP 3: Create Tables in Both Databases
    // =======================================================
    private static void initDatabases() throws SQLException {
        System.out.println("\n-> Creating AUTH database tables...");
        authDb.createTablesIfNotExist();
        System.out.println("   AUTH tables OK.");

        System.out.println("-> Creating ERP database tables...");
        erpDb.createTablesIfNotExist();
        System.out.println("   ERP tables OK.");
    }


    // =======================================================
    //     STEP 4: Insert Test Admin / Courses
    //  - important: ensure admin is ACTIVE and not locked
    // =======================================================
    private static void insertInitialData() throws SQLException {
        System.out.println("\n-> Inserting default data...");

        // ****code for seeding sample users*****

        // // Simple placeholder hash (matches your existing approach)
        // String adminPass = "Admin@123";
        // String adminHash = AuthService.hashPassword(adminPass);

        // int adminId = -1;

        // if (!authDb.usernameExists("admin1")) {
        //     adminId = authDb.addUser("admin1", AuthDatabase.ROLE_ADMIN, adminHash);
        //     System.out.println("   + Added admin1 (user_id: " + adminId + ")");
        // } else {
        //     // If admin1 already exists, fetch its id
        //     var adminOpt = authDb.findByUsername("admin1");
        //     if (adminOpt.isPresent()) {
        //         adminId = adminOpt.get().getUserId();
        //         System.out.println("   + Admin1 already exists (user_id: " + adminId + ")");
        //     }
        // }

        
        // // Ensure admin account is ACTIVE and unlocked (reset failed_attempts & locked_until)
        // if (adminId != -1) {
        //     try {
        //         // Mark status ACTIVE using existing API
        //         authDb.updateStatus(adminId, AuthDatabase.STATUS_ACTIVE);

        //         // Also RESET failed_attempts and locked_until directly (AuthDatabase has no dedicated method)
        //         try (PreparedStatement ps = authConn.prepareStatement(
        //                 "UPDATE users_auth SET failed_attempts = 0, locked_until = NULL WHERE user_id = ?")) {
        //             ps.setInt(1, adminId);
        //             int updated = ps.executeUpdate();
        //             if (updated > 0) {
        //                 System.out.println("   + Admin1 account state reset (failed_attempts=0, locked_until=NULL).");
        //             } else {
        //                 System.out.println("   ! Warning: admin1 account reset update affected 0 rows.");
        //             }
        //         }
        //     } catch (SQLException e) {
        //         System.err.println("! Failed to ensure admin unlocked: " + e.getMessage());
        //         // continue startup - we do not want to break startup solely for this
        //     }
        // }

        SeedDataWithNames.seedCompleteData(authDb, erpDb);
    }


    // =======================================================
    //        STEP 5: Initialize ALL Services
    // UPDATED: Added AccessControl injection into AuthService
    // =======================================================
    private static void initServices() {

        System.out.println("\n-> Initializing services...");

        // 1. Access Control (requires ERPDatabase)
        accessControlService = new AccessControlService(erpDb);

        // 2. Auth Service (requires AuthDatabase)
        authService = new AuthService(authDb);
        // Inject AccessControl for maintenance mode checks in password changes
        authService.setAccessControl(accessControlService);

        // 3. Catalog Service (requires ERPDatabase)
        catalogService = new CatalogService(erpDb);

        // 4. Student Service (requires ERPDatabase + AccessControl)
        studentService = new StudentService(erpDb, accessControlService);

        // 5. Grade Computation
        gradeComputationService = new GradeComputationService();

        // 6. Instructor Service
        instructorService = new InstructorService(
                erpDb,
                accessControlService,
                gradeComputationService
        );

        // 7. Admin Service (requires AuthService in its constructor)
        adminService = new AdminService(
                authDb,
                erpDb,
                authService,
                accessControlService
        );

        System.out.println("-> All services initialized successfully.");
    }




    // =======================================================
    //                STEP 6: Launch Login UI
    // =======================================================
    private static void launchUI() {

        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            System.out.println("\n-> Launching Login UI...");
            new LoginFrame(authService, erpDb).setVisible(true);
        });
    }


    // =======================================================
    //        OPTIONAL: Utility for Authentication
    // =======================================================

    public static StudentService getStudentService() {
        return studentService;
    }

    public static InstructorService getInstructorService() {
        return instructorService;
    }

    public static CatalogService getCatalogService() {
        return catalogService;
    }
    
    public static AdminService getAdminService() {
        return adminService;
    }

    public static AccessControlService getAccessControlService() {
        return accessControlService;
    }

    public static AuthService getAuthService() {
        return authService;
    }

    public static ERPDatabase getERPDatabase() {
        return erpDb;
    }

    public static AuthDatabase getAuthDatabase() {
        return authDb;
    }

    // =======================================================
    //        CLEANUP: Close DB connections
    // =======================================================
    private static void closeConnections() {
        if (authConn != null) {
            try {
                if (!authConn.isClosed()) {
                    authConn.close();
                    System.out.println("   Closed auth DB connection.");
                }
            } catch (SQLException e) {
                System.err.println("   Error closing authConn: " + e.getMessage());
            }
        }
        if (erpConn != null) {
            try {
                if (!erpConn.isClosed()) {
                    erpConn.close();
                    System.out.println("   Closed erp DB connection.");
                }
            } catch (SQLException e) {
                System.err.println("   Error closing erpConn: " + e.getMessage());
            }
        }
    }
}
