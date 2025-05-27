package com.sunway.course.timetable.model.assignment;

public class ModuleSem {
    private final String moduleId;

    public ModuleSem(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getModuleId() {
        return moduleId;
    }

    @Override
    public String toString() {
        return moduleId;
    }

}
