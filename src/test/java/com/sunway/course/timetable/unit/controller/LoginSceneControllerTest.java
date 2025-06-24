package com.sunway.course.timetable.unit.controller;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sunway.course.timetable.controller.authentication.LoginSceneController;
import com.sunway.course.timetable.interfaces.services.UserService;
import com.sunway.course.timetable.service.NavigationService;

import javafx.embed.swing.JFXPanel;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

@ExtendWith(MockitoExtension.class)
public class LoginSceneControllerTest {

    @Mock private UserService userService;
    @Mock private NavigationService navigationService;

    private LoginSceneController controller;

    @BeforeAll
    static void initJavaFx() {
        new JFXPanel(); // initialize JavaFX runtime
    }

    @BeforeEach
    void setUp() {
        controller = new LoginSceneController(navigationService);
        controller.setUserService(userService);

        // Simulate JavaFX fields (you must manually inject them for unit test)
        controller.setUsernameField(new TextField());
        controller.setPasswordField(new PasswordField());

    }

    @Test
    @DisplayName("Test login success with valid admin credentials")
        void testLoginSuccess() throws Exception {
        TextField usernameField = new TextField("admin");
        PasswordField passwordField = new PasswordField();
        passwordField.setText("Admin@2024");

        controller.setUsernameField(usernameField);
        controller.setPasswordField(passwordField);

        when(userService.validateUser("admin", "Admin@2024")).thenReturn(true);

        controller.login();

        verify(navigationService).loadMainPage();
    }

    @Test
    @DisplayName("Test login failure with invalid credentials")
    void testLoginFailure() throws Exception {
        TextField usernameField = new TextField("admin");
        PasswordField passwordField = new PasswordField();
        passwordField.setText("wrongpass");

        controller.setUsernameField(usernameField);
        controller.setPasswordField(passwordField);

        when(userService.validateUser("admin", "wrongpass")).thenReturn(false);

        controller.login();

        verify(navigationService, never()).loadMainPage();
    }

}

