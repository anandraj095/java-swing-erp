package edu.univ.erp.domain;

import java.util.Objects;

/**
 * Settings Domain Model
 * Represents system-wide settings in the ERP Database
 * Key-value pairs for configuration like maintenance mode
 */
public class Settings {
    
    // Fields
    private String settingKey;
    private String settingValue;
    private String description;
    
    // Common setting keys
    public static final String KEY_MAINTENANCE_MODE = "maintenance_mode";
    public static final String KEY_CURRENT_SEMESTER = "current_semester";
    public static final String KEY_CURRENT_YEAR = "current_year";
    public static final String KEY_DROP_DEADLINE = "drop_deadline";
    public static final String KEY_REGISTRATION_OPEN = "registration_open";
    public static final String KEY_MAX_ENROLLMENTS = "max_enrollments_per_student";
    public static final String KEY_MIN_ENROLLMENT = "min_enrollment_for_section";
    public static final String KEY_SYSTEM_MESSAGE = "system_message";
    
    // Boolean values
    public static final String VALUE_TRUE = "true";
    public static final String VALUE_FALSE = "false";
    
    // Constructors
    public Settings() {
    }
    
    public Settings(String settingKey, String settingValue) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
    }
    
    public Settings(String settingKey, String settingValue, String description) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.description = description;
    }
    
    // Getters and Setters
    public String getSettingKey() {
        return settingKey;
    }
    
    public void setSettingKey(String settingKey) {
        if (settingKey == null || settingKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Setting key cannot be empty");
        }
        if (settingKey.length() > 100) {
            throw new IllegalArgumentException("Setting key cannot exceed 100 characters");
        }
        this.settingKey = settingKey.trim().toLowerCase();
    }
    
    public String getSettingValue() {
        return settingValue;
    }
    
    public void setSettingValue(String settingValue) {
        if (settingValue == null) {
            throw new IllegalArgumentException("Setting value cannot be null");
        }
        this.settingValue = settingValue;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        if (description != null && !description.trim().isEmpty()) {
            this.description = description.trim();
        } else {
            this.description = description;
        }
    }
    
    // Business Logic Methods
    
    /**
     * Check if setting has description
     * @return true if description is set
     */
    public boolean hasDescription() {
        return description != null && !description.isEmpty();
    }
    
    /**
     * Get boolean value of setting
     * @return true if value is "true" (case-insensitive)
     */
    public boolean getBooleanValue() {
        return VALUE_TRUE.equalsIgnoreCase(settingValue);
    }
    
    /**
     * Set boolean value
     * @param value Boolean value to set
     */
    public void setBooleanValue(boolean value) {
        this.settingValue = value ? VALUE_TRUE : VALUE_FALSE;
    }
    
    /**
     * Get integer value of setting
     * @return Integer value or 0 if not parseable
     */
    public int getIntValue() {
        try {
            return Integer.parseInt(settingValue);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * Set integer value
     * @param value Integer value to set
     */
    public void setIntValue(int value) {
        this.settingValue = String.valueOf(value);
    }
    
    /**
     * Get double value of setting
     * @return Double value or 0.0 if not parseable
     */
    public double getDoubleValue() {
        try {
            return Double.parseDouble(settingValue);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    /**
     * Set double value
     * @param value Double value to set
     */
    public void setDoubleValue(double value) {
        this.settingValue = String.valueOf(value);
    }
    
    /**
     * Check if this is the maintenance mode setting
     * @return true if key is maintenance_mode
     */
    public boolean isMaintenanceSetting() {
        return KEY_MAINTENANCE_MODE.equals(settingKey);
    }
    
    /**
     * Check if this is a boolean setting
     * @return true if value is "true" or "false"
     */
    public boolean isBooleanSetting() {
        return VALUE_TRUE.equalsIgnoreCase(settingValue) || 
               VALUE_FALSE.equalsIgnoreCase(settingValue);
    }
    
    /**
     * Check if this is a numeric setting
     * @return true if value can be parsed as number
     */
    public boolean isNumericSetting() {
        try {
            Double.parseDouble(settingValue);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Get display name for setting
     * @return Formatted key name
     */
    public String getDisplayName() {
        if (settingKey == null) return "Unknown Setting";
        // Convert snake_case to Title Case
        String[] parts = settingKey.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (sb.length() > 0) sb.append(" ");
            if (part.length() > 0) {
                sb.append(Character.toUpperCase(part.charAt(0)))
                  .append(part.substring(1));
            }
        }
        return sb.toString();
    }
    
    /**
     * Get display value with proper formatting
     * @return Formatted value
     */
    public String getDisplayValue() {
        if (isBooleanSetting()) {
            return getBooleanValue() ? "Enabled" : "Disabled";
        }
        return settingValue;
    }
    
    /**
     * Get full display with description
     * @return Complete setting information
     */
    public String getFullDisplay() {
        StringBuilder sb = new StringBuilder(getDisplayName());
        sb.append(": ").append(getDisplayValue());
        if (hasDescription()) {
            sb.append(" (").append(description).append(")");
        }
        return sb.toString();
    }
    
    /**
     * Validate all required fields are present
     * @return true if all required fields are valid
     */
    public boolean isValid() {
        return settingKey != null && !settingKey.isEmpty() &&
               settingValue != null;
    }
    
    /**
     * Toggle boolean setting
     * @return New boolean value
     * @throws IllegalStateException if not a boolean setting
     */
    public boolean toggleBoolean() {
        if (!isBooleanSetting()) {
            throw new IllegalStateException("Cannot toggle non-boolean setting: " + settingKey);
        }
        boolean newValue = !getBooleanValue();
        setBooleanValue(newValue);
        return newValue;
    }
    
    /**
     * Check if value matches
     * @param value Value to check
     * @return true if matches (case-insensitive)
     */
    public boolean valueEquals(String value) {
        return settingValue != null && settingValue.equalsIgnoreCase(value);
    }
    
    /**
     * Check if value contains substring
     * @param substring Substring to check
     * @return true if contains (case-insensitive)
     */
    public boolean valueContains(String substring) {
        return settingValue != null && 
               settingValue.toLowerCase().contains(substring.toLowerCase());
    }
    
    // Static factory methods for common settings
    
    /**
     * Create maintenance mode setting
     * @param enabled Whether maintenance is enabled
     * @return Settings object
     */
    public static Settings maintenanceMode(boolean enabled) {
        return new Settings(
            KEY_MAINTENANCE_MODE, 
            enabled ? VALUE_TRUE : VALUE_FALSE,
            "System maintenance mode flag"
        );
    }
    
    /**
     * Create current semester setting
     * @param semester Semester name
     * @return Settings object
     */
    public static Settings currentSemester(String semester) {
        return new Settings(
            KEY_CURRENT_SEMESTER,
            semester,
            "Current active semester"
        );
    }
    
    /**
     * Create current year setting
     * @param year Year value
     * @return Settings object
     */
    public static Settings currentYear(int year) {
        return new Settings(
            KEY_CURRENT_YEAR,
            String.valueOf(year),
            "Current academic year"
        );
    }
    
    /**
     * Create registration open setting
     * @param open Whether registration is open
     * @return Settings object
     */
    public static Settings registrationOpen(boolean open) {
        return new Settings(
            KEY_REGISTRATION_OPEN,
            open ? VALUE_TRUE : VALUE_FALSE,
            "Registration period status"
        );
    }
    
    // Utility Methods
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Settings settings = (Settings) o;
        return Objects.equals(settingKey, settings.settingKey);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(settingKey);
    }
    
    @Override
    public String toString() {
        return "Settings{" +
                "key='" + settingKey + '\'' +
                ", value='" + settingValue + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
    
    /**
     * Create a copy of this settings object
     * @return New Settings object with same values
     */
    public Settings copy() {
        Settings copy = new Settings();
        copy.settingKey = this.settingKey;
        copy.settingValue = this.settingValue;
        copy.description = this.description;
        return copy;
    }
}