package com.sunway.course.timetable.model;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "satisfaction")
public class Satisfaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double score; // Name of the satisfaction

    @Column(nullable = false)
    private int conflict;

    public Satisfaction() {
        // Default constructor
    }

    public Satisfaction(Double score, int conflict) {
        this.score = score;
        this.conflict = conflict;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public int getConflict() {
        return conflict;
    }

    public void setConflict(int conflict) {
        this.conflict = conflict;
    }

}
