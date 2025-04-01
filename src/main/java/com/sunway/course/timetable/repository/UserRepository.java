package com.sunway.course.timetable.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sunway.course.timetable.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional <User> findByUsername(String username);
}
