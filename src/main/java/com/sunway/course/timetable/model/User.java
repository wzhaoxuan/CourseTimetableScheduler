package com.sunway.course.timetable.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = false)
    private String password;

    @Column(nullable = false)
    private boolean isLogin;


    // Constructors
    public User() {}

    public User(String email, String password, boolean isLogin) {
        this.email = email;
        this.password = password;
        this.isLogin = isLogin;
    }

    // Getters and Setters
    public Long getId() { return Id; }
    public void setId(Long id) { Id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email;}

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password;}

    public boolean isLogin() { return isLogin; }
    public void setLogin(boolean login) { isLogin = login; }

}
