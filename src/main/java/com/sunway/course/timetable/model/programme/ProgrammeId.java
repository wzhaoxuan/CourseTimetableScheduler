package com.sunway.course.timetable.model.programme;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ProgrammeId implements Serializable{

    @Column(name = "id")
    private Long id;

    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "module_id")
    private Long moduleId;

    public ProgrammeId() {
        // Default constructor
    }

    public ProgrammeId(Long id, Long studentId, Long moduleId) {
        this.id = id;
        this.studentId = studentId;
        this.moduleId = moduleId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getModuleId() {
        return moduleId;
    }

    public void setModuleId(Long moduleId) {
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
