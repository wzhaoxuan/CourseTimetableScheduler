package com.sunway.course.timetable.controller.authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunway.course.timetable.controller.base.AuthBaseController;
import com.sunway.course.timetable.service.NavigationService;
import com.sunway.course.timetable.service.UserService;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

@Component // Let Spring manage the controller
public class LoginSceneController extends AuthBaseController {

    @FXML private Button loginButton;

    @Autowired
    UserService userService; // Autowire UserService

    // Inject MainApp using constructor
    public LoginSceneController(NavigationService navService) { 
        super(navService); // Call the superclass constructor
    }

    @Override
    protected void initialize(){
        super.initialize(); // Call BaseController's initialize()
        loginButton.setText("Login");
        loginButton.setDefaultButton(true);
    }

    @FXML
    private void login() {
        username = trimUsername();
        password = trimPassword();

        if(userService.validateUser(username, password)) {
            System.out.println("Login successful");
            navigateToPage("MainPage"); // Navigate to the main page
        } else {
            System.out.println("Login failed");
        }
    }

    @Override
    protected void signUp() {
        System.out.println("Sign up clicked");
        navigateToPage("SignUpPage"); // Navigate to the sign-up page
    }

    public String getUsername() {
        return username;
    }
}
