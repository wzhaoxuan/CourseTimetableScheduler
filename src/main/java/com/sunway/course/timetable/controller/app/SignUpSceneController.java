package com.sunway.course.timetable.controller.app;

import org.springframework.stereotype.Component;

import com.sunway.course.timetable.view.MainApp;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;


@Component
public class SignUpSceneController extends AuthBaseController {

    @FXML
    private PasswordField confirmPasswordField;

    public SignUpSceneController( MainApp mainApp) { // Prevents premature injection
        super(mainApp); 
    }

    @Override
    protected void initialize(){
        super.initialize(); // Call BaseController's initialize()
        confirmPasswordField.setPromptText("Confirm Password");
    }

    @Override
    protected void signUp() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if(username.equals("admin") && password.equals("admin123")){
            if(confirmPassword.equals(password)){
                System.out.println("SignUp successful");
                try {
                    MainApp.getInstance().loadLoginPage(); // Handle exception properly
                } catch (Exception e) {
                    e.printStackTrace(); // Print the error if something goes wrong
                }
            } else {
                System.out.println("Passwords do not match");
            }
        } else {
            System.out.println("SignUp failed");
        }
    }
}
