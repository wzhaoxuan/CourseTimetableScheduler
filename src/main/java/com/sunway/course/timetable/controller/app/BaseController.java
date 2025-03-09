package com.sunway.course.timetable.controller.app;

import org.springframework.beans.factory.annotation.Autowired;

import com.sunway.course.timetable.view.MainApp;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public abstract class BaseController {

    @FXML
    protected ImageView profile;

    @FXML
    protected Label username, title;

    @FXML
    protected Button homeButton, generateTimetable, viewTimetable, logOutButton;

    @Autowired
    protected LoginSceneController loginSceneController;

    protected final MainApp mainApp;

    public BaseController(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    protected void initialize() {
        title.setText("SCTS");
        homeButton.setText("Home");
        generateTimetable.setText("Generate Timetable");
        viewTimetable.setText("View Timetable");
        logOutButton.setText("Log Out");

        String uppercase_username = loginSceneController.getUsername();
        username.setText(uppercase_username.substring(0, 1).toUpperCase() + uppercase_username.substring(1));

        setButtonHoverEffect(homeButton);
        setButtonHoverEffect(generateTimetable);
        setButtonHoverEffect(viewTimetable);
        setButtonHoverEffect(logOutButton);
    }

    @FXML
    protected void home() {
        try {
            MainApp.getInstance().loadMainPage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void generateTimetable() {
        try {
            MainApp.getInstance().loadGenerateTimetablePage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void viewTimetable() {
        try {
            MainApp.getInstance().loadViewTimetablePage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void logOut() {
        try {
            MainApp.getInstance().loadLoginPage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void setButtonHoverEffect(Button button) {
        button.setOnMouseEntered(e -> button.setCursor(Cursor.HAND));
        button.setOnMouseExited(e -> button.setCursor(Cursor.DEFAULT));
    }

}
