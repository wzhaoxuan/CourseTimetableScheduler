package com.sunway.course.timetable.model.programme;
import com.sunway.course.timetable.model.Module;
import com.sunway.course.timetable.model.Student;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "programme")
public class Programme {

    @EmbeddedId
    private ProgrammeId programmeId;

    @ManyToOne
    @MapsId("studentId") // Maps studentId from the embedded composite key
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private Student student;

    @ManyToOne
    @MapsId("moduleId") // Maps moduleId from the embedded composite key
    @JoinColumn(name = "module_id", insertable = false, updatable = false)
    private Module module;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false)
    private String intake;

    @Column(nullable = false)
    private int duration;

    // Default constructor
    public Programme() {}

    public Programme(ProgrammeId programmeId, String name, int year, String intake, int duration) {
        this.programmeId = programmeId;
        this.name = name;
        this.year = year;
        this.intake = intake;
        this.duration = duration;
    }

    public ProgrammeId getProgrammeId() {
        return programmeId;
    }

    public void setProgrammeId(ProgrammeId programmeId) {
        this.programmeId = programmeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getIntake() {
        return intake;
    }

    public void setIntake(String intake) {
        this.intake = intake;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }


}
