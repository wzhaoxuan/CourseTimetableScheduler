package com.sunway.course.timetable.model.programme;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ProgrammeId implements Serializable{

    @Column(name = "id")
    private String id;

    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "module_id")
    private String moduleId;

    public ProgrammeId() {
        // Default constructor
    }

    public ProgrammeId(String id, Long studentId, String moduleId) {
        this.id = id;
        this.studentId = studentId;
        this.moduleId = moduleId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProgrammeId)) return false;
        ProgrammeId that = (ProgrammeId) o;
        return id.equals(that.id) && studentId.equals(that.studentId) && moduleId.equals(that.moduleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, studentId, moduleId);
    }

}
