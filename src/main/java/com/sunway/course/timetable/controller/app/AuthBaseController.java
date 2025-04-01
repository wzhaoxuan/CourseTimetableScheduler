package com.sunway.course.timetable.controller.app;

import com.sunway.course.timetable.view.MainApp;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public abstract class AuthBaseController {

    protected final MainApp mainApp;

    @FXML
    protected TextField usernameField;

    @FXML
    protected PasswordField passwordField;

    @FXML
    private Button signUpButton;

    @FXML
    private Label title;

    @FXML 
    private Label description;

    @FXML
    private ImageView logo;

    public AuthBaseController(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    protected void initialize(){
        title.setText(mainApp.getTitle());
        description.setText("Empowering smarter\nscheduling with\nAI precision");
        signUpButton.setText("Sign Up");

        usernameField.setPromptText("UserID");
        passwordField.setPromptText("Password");
        logo.setImage(new Image(getClass().getResourceAsStream(mainApp.getIcon())));

        setButtonHoverEffect(signUpButton);
        
    }

    protected void setButtonHoverEffect(Button button) {
        button.setOnMouseEntered(e -> button.setCursor(Cursor.HAND));
        button.setOnMouseExited(e -> button.setCursor(Cursor.DEFAULT));
    }

    
    @FXML
    protected abstract void signUp();

}
