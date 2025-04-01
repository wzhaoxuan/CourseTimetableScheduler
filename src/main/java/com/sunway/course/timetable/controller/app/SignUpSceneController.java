package com.sunway.course.timetable.controller.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunway.course.timetable.model.User;
import com.sunway.course.timetable.service.UserService;
import com.sunway.course.timetable.view.MainApp;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;


@Component
public class SignUpSceneController extends AuthBaseController {

    @FXML
    private PasswordField confirmPasswordField;

    @Autowired
    private UserService userService;

    public SignUpSceneController(MainApp mainApp) { // Prevents premature injection
        super(mainApp); 
    }

    @Override
    protected void initialize(){
        super.initialize(); 
        confirmPasswordField.setPromptText("Confirm Password");
    }

    @Override
    protected void signUp() {
        username = trimUsername();
        password = trimPassword();
        String confirmPassword = confirmPasswordField.getText().trim();

        if(username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()){
            System.out.println("All fields are required!");
        }

        if(!password.equals(confirmPassword)){
            System.out.println("Password does not match!");
        }

        if(userService.findByUsername(username).isPresent()){
            System.out.println("User already exists");
        }

        if(!userService.findByUsername(username).isPresent() && password.equals(confirmPassword)){
            User newUser = new User(username, password, false);
            userService.addUser(newUser);
            System.out.println("Sign Up Successfully");

            try {
                MainApp.getInstance().loadLoginPage(); // Handle exception properly
            } catch (Exception e) {
                e.printStackTrace(); // Print the error if something goes wrong
            }
        }
    }
}
