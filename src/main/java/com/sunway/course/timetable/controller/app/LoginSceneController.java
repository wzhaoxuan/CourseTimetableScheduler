package com.sunway.course.timetable.controller.app;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.sunway.course.timetable.view.MainApp;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

@Component // Let Spring manage the controller
public class LoginSceneController{

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button signUpButton;

    @FXML
    private Label title;

    @FXML 
    private Label description;

    @FXML
    private ImageView logo;

    private final MainApp mainApp; // Inject MainApp using constructor

    public LoginSceneController(@Lazy MainApp mainApp) { // Prevents premature injection
        this.mainApp = mainApp; 
    }

    @FXML
    private void initialize(){
        title.setText(mainApp.getTitle());
        description.setText("Empowering smarter\nscheduling with\nAI precision");
        loginButton.setText("Login");
        signUpButton.setText("Sign Up");
        setButtonHoverEffect(loginButton);
        setButtonHoverEffect(signUpButton);

        usernameField.setPromptText("UserID");
        passwordField.setPromptText("Password");
        logo.setImage(new Image(getClass().getResourceAsStream(mainApp.getIcon())));
    }

    private void setButtonHoverEffect(Button button) {
        button.setOnMouseEntered(e -> button.setCursor(Cursor.HAND));
        button.setOnMouseExited(e -> button.setCursor(Cursor.DEFAULT));
    }

    @FXML
    private void login() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if(username.equals("admin") && password.equals("admin123")){
            System.out.println("Login successful");
            try {
                mainApp.loadMainPage(); // Handle exception properly
            } catch (Exception e) {
                e.printStackTrace(); // Print the error if something goes wrong
            }
        } else {
            System.out.println("Login failed");
        }
    }

    @FXML
    private void signUp() {
        System.out.println("Sign up clicked");
    }
}
