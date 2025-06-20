package com.sunway.course.timetable.result;

public class LecturerVersionItem {
    private final String name;
    private final String version;

    public LecturerVersionItem(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public String getDisplayName() {
        return name + " (" + version + ")";
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }
}

