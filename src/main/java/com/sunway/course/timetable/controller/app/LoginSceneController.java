package com.sunway.course.timetable.controller.app;

import org.springframework.stereotype.Component;

import com.sunway.course.timetable.view.MainApp;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

@Component // Let Spring manage the controller
public class LoginSceneController extends AuthBaseController {

    @FXML
    private Button loginButton;

     // Inject MainApp using constructor
    private String username;
    private String password;

    public LoginSceneController(MainApp mainApp) { // Prevents premature injection
        super(mainApp); // Call the superclass constructor
    }

    @Override
    protected void initialize(){
        super.initialize(); // Call BaseController's initialize()
        loginButton.setText("Login");
        setButtonHoverEffect(loginButton);
    }

    @FXML
    private void login() {
        username = usernameField.getText();
        password = passwordField.getText();

        if(username.equals("admin") && password.equals("admin123")){
            System.out.println("Login successful");
            try {
                MainApp.getInstance().loadMainPage(); // Handle exception properly
            } catch (Exception e) {
                e.printStackTrace(); // Print the error if something goes wrong
            }
        } else {
            System.out.println("Login failed");
        }
    }

    @Override
    protected void signUp() {
        System.out.println("Sign up clicked");
        try {
            MainApp.getInstance().loadSignUpPage(); // Handle exception properly
        } catch (Exception e) {
            e.printStackTrace(); // Print the error if something goes wrong
        }
    }

    public String getUsername() {
        return username;
    }
}
