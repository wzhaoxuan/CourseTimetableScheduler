package com.sunway.course.timetable.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.sunway.course.timetable.model.User;
import com.sunway.course.timetable.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public List<User> getAllUser() {
        return userRepository.findAll();
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User addUser(User user) {
        // Hash the password before saving it to the database
        String hashedPassword = encoder.encode(user.getPassword());
        user.setPassword(hashedPassword);
        return userRepository.save(user);
    }

    public boolean validateUser(String username, String enteredPassword) {
        // Fetch the user from the database
       Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return encoder.matches(enteredPassword, user.getPassword());
        }
        return false;
    }
}
