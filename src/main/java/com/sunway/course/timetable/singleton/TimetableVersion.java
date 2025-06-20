package com.sunway.course.timetable.singleton;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimetableVersion {
    private static final String versionTag = 
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

    public static String getVersionTag() {
        return versionTag;
    }
}
