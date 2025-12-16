package edu.univ.erp.domain;

/**
 * Admin Domain Model
 * Represents an administrative user in the ERP system
 */
public class Admin {
    private int adminId;
    private int userId;
    private String adminCode;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String department;
    private String designation;
    
    // Constructors
    public Admin() {}
    
    public Admin(int adminId, int userId, String adminCode, String firstName, String lastName,
                String email, String phone, String department, String designation) {
        this.adminId = adminId;
        this.userId = userId;
        this.adminCode = adminCode;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.designation = designation;
    }
    
    // Getters and Setters
    public int getAdminId() { 
        return adminId; 
    }
    
    public void setAdminId(int adminId) { 
        this.adminId = adminId; 
    }
    
    public int getUserId() { 
        return userId; 
    }
    
    public void setUserId(int userId) { 
        this.userId = userId; 
    }
    
    public String getAdminCode() { 
        return adminCode; 
    }
    
    public void setAdminCode(String adminCode) { 
        this.adminCode = adminCode; 
    }
    
    public String getFirstName() { 
        return firstName; 
    }
    
    public void setFirstName(String firstName) { 
        this.firstName = firstName; 
    }
    
    public String getLastName() { 
        return lastName; 
    }
    
    public void setLastName(String lastName) { 
        this.lastName = lastName; 
    }
    
    public String getEmail() { 
        return email; 
    }
    
    public void setEmail(String email) { 
        this.email = email; 
    }
    
    public String getPhone() { 
        return phone; 
    }
    
    public void setPhone(String phone) { 
        this.phone = phone; 
    }
    
    public String getDepartment() { 
        return department; 
    }
    
    public void setDepartment(String department) { 
        this.department = department; 
    }
    
    public String getDesignation() { 
        return designation; 
    }
    
    public void setDesignation(String designation) { 
        this.designation = designation; 
    }
    
    // Utility Methods
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    @Override
    public String toString() {
        return "Admin{" +
                "adminId=" + adminId +
                ", adminCode='" + adminCode + '\'' +
                ", name='" + getFullName() + '\'' +
                ", email='" + email + '\'' +
                ", department='" + department + '\'' +
                '}';
    }
}
