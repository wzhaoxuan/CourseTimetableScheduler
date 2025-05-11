package com.sunway.course.timetable.interfaces.services;

import java.util.List;
import java.util.Optional;

import com.sunway.course.timetable.model.User;

public interface UserService {
    List<User> getAllUser();
    Optional<User> getUserById(Long id);
    Optional<User> findByUsername(String username);
    User addUser(String username, String password);
    boolean validateUser(String username, String enteredPassword);
    boolean validateSignUpField(String username, String password, String confirmPassword);
    void deleteUser(Long id);
    User updateUser(Long id, User user);
}
