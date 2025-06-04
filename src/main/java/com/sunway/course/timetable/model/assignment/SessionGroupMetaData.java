package com.sunway.course.timetable.model.assignment;


public class SessionGroupMetaData {
    private int semester;
    private final String moduleId;
    private final String type;
    private final String typeGroup;
    private final String lecturerName;
    private final int totalStudents;
    private final int groupIndex;
    private final int groupCount;

    /**
     * Constructor for SessionGroupMetaData.
     *
     * @param semester      The semester number.
     * @param moduleId      The ID of the module.
     * @param type          The type of session (e.g., lecture, tutorial).
     * @param typeGroup     The group type for the session.
     * @param lecturerName    The ID of the lecturer assigned to the session.
     * @param totalStudents The total number of students in the session group.
     *  @param assignedStudents The list of students assigned to this session group.
     *  @param groupIndex   The index of this group within the total groups.
     *  @param groupCount   The total number of groups for this session type.
     */
    public SessionGroupMetaData(int semester, String moduleId, String type, String typeGroup, String lecturerName, int totalStudents, 
                                int groupIndex, int groupCount) {
        this.semester = semester;
        this.moduleId = moduleId;
        this.type = type;
        this.typeGroup = typeGroup;
        this.lecturerName = lecturerName;
        this.totalStudents = totalStudents;
        this.groupIndex = groupIndex;
        this.groupCount = groupCount;
}

    public int getSemester() { return semester; }
    public String getModuleId() { return moduleId; }
    public String getType() { return type; }
    public String getTypeGroup() { return typeGroup; }
    public String getLecturerName() { return lecturerName; }
    public int getTotalStudents() { return totalStudents; }
    public int getGroupIndex() { return groupIndex; }
    public int getGroupCount() { return groupCount; }

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

