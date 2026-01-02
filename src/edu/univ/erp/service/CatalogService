package edu.univ.erp.service;

import edu.univ.erp.data.ERPDatabase;
import edu.univ.erp.data.ERPDatabase.*;
import edu.univ.erp.domain.*;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Map;

/**
 * CatalogService - Handles course catalog browsing and searching
 * Updated to support showing all sections regardless of availability
 * FIXED: Removes duplicate course entries when showing "All" semesters by deduplicating based on course_id
 */
public class CatalogService {
    
    private final ERPDatabase erpDb;
    
    public CatalogService(ERPDatabase erpDb) {
        this.erpDb = erpDb;
    }
    
    /**
     * Gets all available courses
     */
    public List<Course> getAllCourses() throws SQLException {
        return erpDb.getAllCourses();
    }
    
    /**
     * Gets course by ID
     */
    public Course getCourseById(int courseId) throws SQLException {
        return erpDb.getCourseById(courseId).orElse(null);
    }
    
    /**
     * Gets course by code
     */
    public Course getCourseByCode(String courseCode) throws SQLException {
        return erpDb.getCourseByCode(courseCode).orElse(null);
    }
    
    /**
     * Searches courses by keyword (code or title)
     */
    public List<Course> searchCourses(String keyword) throws SQLException {
        List<Course> allCourses = erpDb.getAllCourses();
        String lowerKeyword = keyword.toLowerCase();
        
        return allCourses.stream()
            .filter(c -> c.getCourseCode().toLowerCase().contains(lowerKeyword) ||
                        c.getTitle().toLowerCase().contains(lowerKeyword))
            .collect(Collectors.toList());
    }
    
    /**
     * Gets all sections for a semester
     */
    public List<Section> getSectionsBySemester(String semester, int year) throws SQLException {
        return erpDb.getSectionsBySemesterYear(semester, year);
    }
    
    /**
     * FIXED: Gets all sections regardless of availability (for "All" display mode)
     * Now deduplicates sections to show only one section per course_id + section_name + semester + year
     * This prevents showing the same course multiple times when "All" semesters is selected
     */
    public List<Section> getAllSectionsBySemester(String semester, int year) throws SQLException {
        List<Section> allSections;
        
        if (semester == null || "All".equals(semester)) {
            // Get all sections from database
            allSections = erpDb.getAllSections();
        } else {
            allSections = getSectionsBySemester(semester, year);
        }
        
        // FIXED: Deduplicate sections based on unique key (course_id, section_name, semester, year)
        // This prevents showing duplicate entries when "All" is selected
        Map<String, Section> uniqueSections = new LinkedHashMap<>();
        for (Section section : allSections) {
            // Create unique key: courseId-sectionName-semester-year
            String key = section.getCourseId() + "-" + 
                        section.getSectionName() + "-" + 
                        section.getSemester() + "-" + 
                        section.getYear();
            
            // Only keep the first occurrence of each unique section
            if (!uniqueSections.containsKey(key)) {
                uniqueSections.put(key, section);
            }
        }
        
        return new ArrayList<>(uniqueSections.values());
    }
    
    /**
     * FIXED: Gets available sections only (seats available and status is OPEN)
     * Also deduplicates to prevent showing same course multiple times
     */
    public List<Section> getAvailableSections(String semester, int year) throws SQLException {
        List<Section> allSections;
        
        if (semester == null || "All".equals(semester)) {
            // Get all sections from database
            allSections = erpDb.getAllSections();
        } else {
            allSections = getSectionsBySemester(semester, year);
        }
        
        // Filter for available sections
        List<Section> availableSections = allSections.stream()
            .filter(s -> s.hasSeats() && s.isAvailableForRegistration())
            .collect(Collectors.toList());
        
        // FIXED: Deduplicate available sections
        Map<String, Section> uniqueSections = new LinkedHashMap<>();
        for (Section section : availableSections) {
            // Create unique key: courseId-sectionName-semester-year
            String key = section.getCourseId() + "-" + 
                        section.getSectionName() + "-" + 
                        section.getSemester() + "-" + 
                        section.getYear();
            
            // Only keep the first occurrence of each unique section
            if (!uniqueSections.containsKey(key)) {
                uniqueSections.put(key, section);
            }
        }
        
        return new ArrayList<>(uniqueSections.values());
    }
    
    /**
     * Gets section details with enrollment info
     */
    public Section getSectionById(int sectionId) throws SQLException {
        return erpDb.getSectionById(sectionId).orElse(null);
    }
    
    /**
     * Gets sections by course
     */
    public List<Section> getSectionsByCourse(int courseId, String semester, int year) throws SQLException {
        return getSectionsBySemester(semester, year).stream()
            .filter(s -> s.getCourseId() == courseId)
            .collect(Collectors.toList());
    }
}
