package com.sunway.course.timetable.model.assignment;

import java.util.List;
import java.util.Map;

public class PreprocessingResult {
    private final List<ModuleAssignmentData> moduleAssignmentDataList;
    private final Map<Long, String> studentProgrammeMap;
    private final Map<Long, Integer> studentSemesterMap;

    public PreprocessingResult(List<ModuleAssignmentData> moduleAssignmentDataList,
                               Map<Long, String> studentProgrammeMap,
                               Map<Long, Integer> studentSemesterMap) {
        this.moduleAssignmentDataList = moduleAssignmentDataList;
        this.studentProgrammeMap = studentProgrammeMap;
        this.studentSemesterMap = studentSemesterMap;
    }

    public List<ModuleAssignmentData> getModuleAssignmentDataList() {
        return moduleAssignmentDataList;
    }

    public Map<Long, String> getStudentProgrammeMap() {
        return studentProgrammeMap;
    }

    public Map<Long, Integer> getStudentSemesterMap() {
        return studentSemesterMap;
    }
}
