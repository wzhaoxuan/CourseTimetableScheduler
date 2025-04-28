package com.sunway.course.timetable.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.sunway.course.timetable.exception.IdNotFoundException;
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

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User addUser(String username, String password) {
        User newUser = new User(username, password, false);
        // Hash the password before saving it to the database
        String hashedPassword = encoder.encode(newUser.getPassword());
        newUser.setPassword(hashedPassword);
        return userRepository.save(newUser);
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

    public boolean validateSignUpField(String username, String password, String confirmPassword){
        if(username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()){
            System.out.println("All fields are required!");
            return false;
        }

        if(!password.equals(confirmPassword)){
            System.out.println("Password does not match!");
            return false;
        }

        if(findByUsername(username).isPresent()){
            System.out.println("User already exists");
            return false;
        }

        return password.equals(confirmPassword);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    public User updateUser(Long id, User user) {
        if (userRepository.existsById(id)) {
            user.setId(id);
            return userRepository.save(user);
        } else {
            throw new IdNotFoundException("User not found with id: " + id);
        }
    }
}
