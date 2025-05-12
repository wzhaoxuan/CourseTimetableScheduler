package com.sunway.course.timetable.unit.service;

import com.sunway.course.timetable.model.User;
import com.sunway.course.timetable.repository.UserRepository;
import com.sunway.course.timetable.service.UserServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder encoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "password123", false);
    }

    @Test
    @DisplayName("Add User - Should hash password and save")
    void testAddUser() {
        String hashedPassword = "hashedPassword";
        when(encoder.encode(testUser.getPassword())).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.addUser(testUser.getUsername(), testUser.getPassword());

        assertEquals("testuser", result.getUsername());
        assertEquals(hashedPassword, result.getPassword());
        verify(encoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Validate User - Correct password")
    void testValidateUserSuccess() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(encoder.matches("password123", "password123")).thenReturn(true);

        boolean result = userService.validateUser("testuser", "password123");
        assertTrue(result);
    }

    @Test
    @DisplayName("Validate User - Incorrect password")
    void testValidateUserWrongPassword() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(encoder.matches("wrong", "password123")).thenReturn(false);

        boolean result = userService.validateUser("testuser", "wrong");
        assertFalse(result);
    }

    @Test
    @DisplayName("Validate User - User not found")
    void testValidateUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        boolean result = userService.validateUser("unknown", "pass");
        assertFalse(result);
    }

    @Test
    @DisplayName("Validate SignUp Field - All Valid")
    void testValidateSignUpFieldValid() {
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        boolean result = userService.validateSignUpField("newuser", "pass", "pass");
        assertTrue(result);
    }

    @Test
    @DisplayName("Validate SignUp Field - Empty Fields")
    void testValidateSignUpFieldEmpty() {
        boolean result = userService.validateSignUpField("", "pass", "pass");
        assertFalse(result);
    }

    @Test
    @DisplayName("Validate SignUp Field - Password Mismatch")
    void testValidateSignUpFieldMismatch() {
        boolean result = userService.validateSignUpField("user", "pass1", "pass2");
        assertFalse(result);
    }

    @Test
    @DisplayName("Validate SignUp Field - Username Exists")
    void testValidateSignUpFieldUserExists() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        boolean result = userService.validateSignUpField("testuser", "pass", "pass");
        assertFalse(result);
    }

    @Test
    @DisplayName("Update User - Exists")
    void testUpdateUserExists() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = new User("newuser", "newpass", false);
        User result = userService.updateUser(1L, updated);

        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("Update User - Not Exists")
    void testUpdateUserNotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);
        User updated = new User("newuser", "newpass", false);

        assertThrows(RuntimeException.class, () -> userService.updateUser(1L, updated));
    }

    @Test
    @DisplayName("Delete User")
    void testDeleteUser() {
        doNothing().when(userRepository).deleteById(1L);
        userService.deleteUser(1L);
        verify(userRepository).deleteById(1L);
    }
}
