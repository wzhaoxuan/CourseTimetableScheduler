package com.sunway.course.timetable.result;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.sunway.course.timetable.model.assignment.ModuleAssignmentData;

/**
 * Holds the module assignment data for the application.
 * This class is used to store and retrieve module assignment data across different components.
 */
@Component
public class ModuleDataHolder {
    private List<ModuleAssignmentData> moduleDataList = new ArrayList<>();

    public void store(List<ModuleAssignmentData> data) {
        this.moduleDataList = data;
    }

    public List<ModuleAssignmentData> getModuleDataList() {
        return this.moduleDataList;
    }
}


