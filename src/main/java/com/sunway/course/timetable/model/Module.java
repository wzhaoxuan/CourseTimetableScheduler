package com.sunway.course.timetable.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "module")
public class Module {

    @Id
    @Column(length = 15, nullable = false)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int creditHour;

    public Module() {
        // Default constructor
    }

    public Module(String id, String name, int creditHour) {
        this.id = id;
        this.name = name;
        this.creditHour = creditHour;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getCreditHour() {
        return creditHour;
    }
    public void setCreditHour(int creditHour) {
        this.creditHour = creditHour;
    }

}
