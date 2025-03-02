package com.sunway.course.timetable.controller.app;

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

@Component
public class SignUpSceneController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button signUpButton;

    @FXML
    private Label title;

    @FXML 
    private Label description;

    @FXML
    private ImageView logo;

    private final MainApp mainApp; // Inject MainApp using constructor

    public SignUpSceneController( MainApp mainApp) { // Prevents premature injection
        this.mainApp = mainApp; 
    }

    @FXML
    private void initialize(){
        title.setText(mainApp.getTitle());
        description.setText("Empowering smarter\nscheduling with\nAI precision");
        signUpButton.setText("Sign Up");
        setButtonHoverEffect(signUpButton);

        usernameField.setPromptText("UserID");
        passwordField.setPromptText("Password");
        confirmPasswordField.setPromptText("Confirm Password");
        logo.setImage(new Image(getClass().getResourceAsStream(mainApp.getIcon())));
    }

    private void setButtonHoverEffect(Button button) {
        button.setOnMouseEntered(e -> button.setCursor(Cursor.HAND));
        button.setOnMouseExited(e -> button.setCursor(Cursor.DEFAULT));
    }

    @FXML
    private void signUp() {
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
