package com.sunway.course.timetable.model.assignment;

public class StudentSem {

    private final int studentId;
    private final String programme;
    private final int semester;  // added semester field

    public StudentSem(int studentId, String programme, int semester) {
        this.studentId = studentId;
        this.programme = programme;
        this.semester = semester;
    }

    public int getStudentId() {
        return studentId;
    }

    public String getProgramme() {
        return programme;
    }

    public int getSemester() {
        return semester;
    }

    @Override
    public String toString() {
        return studentId + " (" + programme + ") Sem: " + semester;
    }
}

