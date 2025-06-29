package com.sunway.course.timetable.controller.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunway.course.timetable.controller.base.AuthBaseController;
import com.sunway.course.timetable.interfaces.services.UserService;
import com.sunway.course.timetable.service.NavigationService;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;


@Component
public class SignUpSceneController extends AuthBaseController {

    @FXML private PasswordField confirmPasswordField;
    @FXML private Button toLoginButton;

    @Autowired
    private UserService userService;

    public SignUpSceneController(NavigationService navService) { 
        super(navService); // Call the superclass constructor
    }

    @Override
    protected void initialize(){
        super.initialize(); 
        toLoginButton.setText("Back to Login");
        confirmPasswordField.setPromptText("Confirm Password");
    }

    @Override
    protected void signUp() {
        username = trimUsername();
        password = trimPassword();
        String confirmPassword = confirmPasswordField.getText().trim();

        if(userService.validateSignUpField(username, password, confirmPassword)) {
            userService.addUser(username, password);
            navigateToPage("LoginPage"); // Navigate to the login page
        } else {
            System.out.println("Sign up failed");
        }
    }

    @FXML
    protected void navigateToLogin() {
        navigateToPage("LoginPage"); // Navigate to the login page
    }
}
