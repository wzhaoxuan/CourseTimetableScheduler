package com.sunway.course.timetable.controller.app;

import com.sunway.course.timetable.view.MainApp;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public abstract class AuthBaseController extends BaseController{

    protected String username;
    protected String password;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button signUpButton;

    @FXML 
    private Label description;

    @FXML
    private ImageView logo;

    public AuthBaseController(MainApp mainApp) {
        super(mainApp);
    }

    @Override
    protected void initialize(){
        title.setText(mainApp.getTitle());
        description.setText("Empowering smarter\nscheduling with\nAI precision");
        signUpButton.setText("Sign Up");

        usernameField.setPromptText("UserID");
        passwordField.setPromptText("Password");
        logo.setImage(new Image(getClass().getResourceAsStream(mainApp.getIcon())));

        setButtonHoverEffect(signUpButton);
    }
    
    @FXML
    protected abstract void signUp();

    protected String trimUsername() {
        return usernameField.getText().trim();
    }

    protected String trimPassword() {
        return passwordField.getText().trim();
    }

}
