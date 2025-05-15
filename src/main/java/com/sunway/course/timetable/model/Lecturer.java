package com.sunway.course.timetable.model;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "lecturer")
public class Lecturer {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String type;


    public Lecturer(){}

    public Lecturer(String name, String email, String type) {
        this.name = name;
        this.email = email;
        this.type = type;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id;}
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

}
