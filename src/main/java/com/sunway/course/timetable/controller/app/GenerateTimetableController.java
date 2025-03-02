package com.sunway.course.timetable.controller.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunway.course.timetable.view.MainApp;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

@Component
public class GenerateTimetableController {

    @FXML
    private ImageView profile;

    @FXML
    private Label username;
    
    @FXML
    private Label title;

    @FXML
    private Label subheading;

    @FXML
    private Label programme;
    
    @FXML
    private Label year;

    @FXML
    private Label intake;

    @FXML
    private Label semester;

    @FXML
    private ChoiceBox<String> programmeChoice;

    @FXML
    private ChoiceBox<String> yearChoice;

    @FXML
    private ChoiceBox<String> intakeChoice;

    @FXML
    private ChoiceBox<String> semesterChoice;

    @FXML
    private Label venue;

    @FXML
    private TextField venueField;

    @FXML
    private Region spacer1, spacer2, spacer3;

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

    //Constructor to prevent premature injection
    public GenerateTimetableController(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void initialize(){
        // Make the spacers expand, pushing the VBoxes apart
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        HBox.setHgrow(spacer3, Priority.ALWAYS);

        String uppercase_username = loginSceneController.getUsername();
        username.setText(uppercase_username.substring(0, 1).toUpperCase() + uppercase_username.substring(1));
        title.setText("SCTS");
        subheading.setText("Generate Timetable");
        homeButton.setText("Home");
        generateTimetable.setText("Generate Timetable");
        viewTimetable.setText("View Timetable");
        logOutButton.setText("Log Out");
        programme.setText("Programme:");
        year.setText("Year:");
        intake.setText("Intake:");
        semester.setText("Semester:");
        venue.setText("Venue:");

        venueField.setPromptText("UW 2-5");

        programmeChoice.getItems().addAll("Diploma in Information Technology", "Diploma in Business Administration", "Diploma in Communication");
        yearChoice.getItems().addAll("2022", "2023", "2024");
        intakeChoice.getItems().addAll("January", "April", "August");
        semesterChoice.getItems().addAll("1", "2", "3");

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
