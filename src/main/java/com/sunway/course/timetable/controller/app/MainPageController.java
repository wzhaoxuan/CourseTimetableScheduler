package com.sunway.course.timetable.controller.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunway.course.timetable.view.MainApp;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;


@Component
public class MainPageController {

    @FXML
    private ImageView profile;

    @FXML
    private Label username;
    
    @FXML
    private Label title;

    @FXML
    private Label subheading;

    @FXML
    private Button homeButton;

    @FXML
    private Button generateTimetable;

    @FXML
    private Button viewTimetable;

    @FXML
    private Button logOutButton;

    @Autowired
    private LoginSceneController loginSceneController;

    private final MainApp mainApp;
    

    public MainPageController(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void initialize(){
        title.setText("SCTS");
        subheading.setText("Dashboard");
        homeButton.setText("Home");

        String uppercase_username = loginSceneController.getUsername();
        username.setText(uppercase_username.substring(0, 1).toUpperCase() + uppercase_username.substring(1));
        // profile.setImage(new Image(getClass().getResourceAsStream("resources/desktop/course/timetable/images/Shiba3.jpg")));

        generateTimetable.setText("Generate Timetable");
        viewTimetable.setText("View Timetable");
        logOutButton.setText("Log Out");

        setButtonHoverEffect(homeButton);
        setButtonHoverEffect(generateTimetable);
        setButtonHoverEffect(viewTimetable);
        setButtonHoverEffect(logOutButton);
    }

    @FXML
    private void home() {
        try {
            MainApp.getInstance().loadMainPage();  // Handle exception properly
        } catch (Exception e) {
            e.printStackTrace(); // Print the error if something goes wrong
        }
    }

    @FXML
    private void generateTimetable() {
        try {
            MainApp.getInstance().loadGenerateTimetablePage();  // Handle exception properly
        } catch (Exception e) {
            e.printStackTrace(); // Print the error if something goes wrong
        }
    }

    @FXML
    private void viewTimetable() {
        try {
            MainApp.getInstance().loadViewTimetablePage();  // Handle exception properly
        } catch (Exception e) {
            e.printStackTrace(); // Print the error if something goes wrong
        }
    }

    @FXML
    private void logOut() {
        try {
            MainApp.getInstance().loadLoginPage();  // Handle exception properly
        } catch (Exception e) {
            e.printStackTrace(); // Print the error if something goes wrong
        }
    }

    private void setButtonHoverEffect(Button button) {
        button.setOnMouseEntered(e -> button.setCursor(Cursor.HAND));
        button.setOnMouseExited(e -> button.setCursor(Cursor.DEFAULT));
    }

    

}
