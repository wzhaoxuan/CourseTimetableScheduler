package com.sunway.course.timetable.result;

public class ModuleVersionItem {
    private final String moduleId;
    private final String version;

    public ModuleVersionItem(String moduleId, String version) {
        this.moduleId = moduleId;
        this.version = version;
    }

    public String getModuleId() {
        return moduleId;
    }

    public String getVersion() {
        return version;
    }

    public String getDisplayName() {
        return moduleId + " (" + version + ")";
    }
}

