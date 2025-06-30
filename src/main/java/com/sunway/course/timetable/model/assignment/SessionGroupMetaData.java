package com.sunway.course.timetable.model.assignment;

import java.util.List;

import com.sunway.course.timetable.model.Student;

public class SessionGroupMetaData {
    private int semester;
    private String moduleId;
    private String type;
    private String typeGroup;
    private String lecturerName;
    private int totalStudents;
    private int groupIndex;
    private int groupCount;
    private List<Student> eligibleStudents;

    /**
     * Constructor for SessionGroupMetaData.
     *
     * @param semester      The semester number.
     * @param moduleId      The ID of the module.
     * @param type          The type of session (e.g., lecture, tutorial).
     * @param typeGroup     The group type for the session.
     * @param lecturerName    The ID of the lecturer assigned to the session.
     * @param totalStudents The total number of students in the session group.
     * @param groupIndex   The index of this group within the total groups.
     * @param groupCount   The total number of groups for this session type.
     * @param eligibleStudents List of students eligible for this session group.
     */
    public SessionGroupMetaData(int semester, String moduleId, String type, String typeGroup, String lecturerName, int totalStudents, 
                                int groupIndex, int groupCount, List<Student> eligibleStudents) {
        this.semester = semester;
        this.moduleId = moduleId;
        this.type = type;
        this.typeGroup = typeGroup;
        this.lecturerName = lecturerName;
        this.totalStudents = totalStudents;
        this.groupIndex = groupIndex;
        this.groupCount = groupCount;
        this.eligibleStudents = eligibleStudents;
    }

    public SessionGroupMetaData() {
        // Default constructor for serialization/deserialization
    }

    public int getSemester() { return semester; }
    public String getModuleId() { return moduleId; }
    public String getType() { return type; }
    public String getTypeGroup() { return typeGroup; }
    public String getLecturerName() { return lecturerName; }
    public int getTotalStudents() { return totalStudents; }
    public int getGroupIndex() { return groupIndex; }
    public int getGroupCount() { return groupCount; }
    public List<Student> getEligibleStudents() { return eligibleStudents; }

    public void setSemester(int semester) { this.semester = semester; }
    public void setModuleId(String moduleId) { this.moduleId = moduleId; }
    public void setType(String type) { this.type = type; }
    public void setTypeGroup(String typeGroup) { this.typeGroup = typeGroup; }
    public void setLecturerName(String lecturerName) { this.lecturerName = lecturerName; }
    public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }
    public void setGroupIndex(int groupIndex) { this.groupIndex = groupIndex; }
    public void setGroupCount(int groupCount) { this.groupCount = groupCount; }
    public void setEligibleStudents(List<Student> eligibleStudents) {
        this.eligibleStudents = eligibleStudents;
    }

    /**
     * Clone metadata for a new overflow group with only these students.
     */
    public SessionGroupMetaData withEligibleStudents(List<Student> students) {
        return new SessionGroupMetaData(
            this.semester,
            this.moduleId,
            this.type,
            this.typeGroup,
            this.lecturerName,
            students.size(),
            this.groupIndex,
            this.groupCount,
            students
        );
    }

    /**
     * Clone metadata under a new typeGroup (e.g. append suffix '-X1').
     */
    public SessionGroupMetaData withNewTypeGroup(String newTypeGroup) {
        return new SessionGroupMetaData(
            this.semester,
            this.moduleId,
            this.type,
            newTypeGroup,
            this.lecturerName,
            this.totalStudents,
            this.groupIndex,
            this.groupCount,
            this.eligibleStudents
        );
    }

    @Override
    public String toString() {
        return "SessionGroupMetaData{" +
                "semester=" + semester +
                ", moduleId='" + moduleId + '\'' +
                ", type='" + type + '\'' +
                ", typeGroup='" + typeGroup + '\'' +
                ", lecturerName='" + lecturerName + '\'' +
                ", totalStudents=" + totalStudents  +
                '}';
    }
}

