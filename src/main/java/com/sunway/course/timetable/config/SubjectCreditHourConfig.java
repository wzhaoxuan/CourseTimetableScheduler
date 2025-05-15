package com.sunway.course.timetable.config;

import java.util.Map;

public class SubjectCreditHourConfig {
    
    public static final Map<String, Integer> CREDIT_HOURS_MAP = Map.ofEntries(
        // Two Credit Hours
            Map.entry("MPU3112", 2),
            Map.entry("MPU3122", 2),
            Map.entry("MPU3132", 2),
            Map.entry("MPU3142", 2),
            Map.entry("MPU3212", 2),
            Map.entry("MPU3232", 2),
            Map.entry("MPU3332", 2),
            Map.entry("MPU3422", 2),
            
            // Three Credit Hours
            Map.entry("PRJ3213", 3),
            Map.entry("PRJ3223", 3),

            // Six Credit Hours
            Map.entry("SEG3203", 6)
    );

    public static int getCreditHour(String subjectCode) {
        return CREDIT_HOURS_MAP.getOrDefault(subjectCode.trim().toUpperCase(), 4); // Default to 4 if not found
    }

}
