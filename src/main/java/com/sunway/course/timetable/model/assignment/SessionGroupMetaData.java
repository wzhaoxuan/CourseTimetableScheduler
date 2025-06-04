package com.sunway.course.timetable.model.assignment;
import java.util.List;

import com.sunway.course.timetable.model.Student;

public class SessionGroupMetaData {
    private int semester;
    private final String moduleId;
    private final String type;
    private final String typeGroup;
    private final String lecturerName;
    private final int totalStudents;
    private List<Student> assignedStudents;

    /**
     * Constructor for SessionGroupMetaData.
     *
     * @param semester      The semester number.
     * @param moduleId      The ID of the module.
     * @param type          The type of session (e.g., lecture, tutorial).
     * @param typeGroup     The group type for the session.
     * @param lecturerName    The ID of the lecturer assigned to the session.
     * @param totalStudents The total number of students in the session group.
     */
    public SessionGroupMetaData(int semester, String moduleId, String type, String typeGroup, String lecturerName, int totalStudents, 
                               List<Student> assignedStudents) {
        this.semester = semester;
        this.moduleId = moduleId;
        this.type = type;
        this.typeGroup = typeGroup;
        this.lecturerName = lecturerName;
        this.totalStudents = totalStudents;
        this.assignedStudents = assignedStudents;
}

    public int getSemester() { return semester; }
    public String getModuleId() { return moduleId; }
    public String getType() { return type; }
    public String getTypeGroup() { return typeGroup; }
    public String getLecturerName() { return lecturerName; }
    public int getTotalStudents() { return totalStudents; }
    public void setAssignedStudents(List<Student> students) {
        this.assignedStudents = students;
    }

    public List<Student> getAssignedStudents() {
        return this.assignedStudents;
    }

    @Override
    public String toString() {
        return "SessionGroupMetaData{" +
                "semester=" + semester +
                ", moduleId='" + moduleId + '\'' +
                ", type='" + type + '\'' +
                ", typeGroup='" + typeGroup + '\'' +
                ", lecturerName='" + lecturerName + '\'' +
                ", totalStudents=" + totalStudents +
                ", assignedStudents=" + assignedStudents +
                '}';
    }
}

