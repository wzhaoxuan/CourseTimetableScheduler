package com.sunway.course.timetable.model;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "session")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "lecturer_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Lecturer lecturer;

    @Column(nullable = false)
    private String day;

    @Column(nullable = false)
    private LocalTime startTime; // Start time in 24-hour format (e.g., 1300 for 1:00 PM)

    @Column(nullable = false)
    private LocalTime endTime; // End time in 24-hour format (e.g., 1400 for 2:00 PM)

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String session_group;

    public Session() {
        // Default constructor
    }

    public Session(Student student, Lecturer lecturer, String day, LocalTime startTime, LocalTime endTime, String type, String session_group) {
        this.student = student;
        this.lecturer = lecturer;
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
        this.session_group = session_group;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Student getStudent() {
        return student;
    }
    public void setStudent(Student student) {
        this.student = student;
    }
    public Lecturer getLecturer() {
        return lecturer;
    }
    public void setLecturer(Lecturer lecturer) {
        this.lecturer = lecturer;
    }
    public String getDay() {
        return day;
    }
    public void setDay(String day) {
        this.day = day;
    }
    public LocalTime getStartTime() {
        return startTime;
    }
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }
    public LocalTime getEndTime() {
        return endTime;
    }
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getsession_group() {
        return session_group;
    }
    public void setsession_group(String session_group) {
        this.session_group = session_group;
    }
}
