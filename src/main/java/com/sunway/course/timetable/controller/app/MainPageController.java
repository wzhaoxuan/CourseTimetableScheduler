package com.sunway.course.timetable.controller.app;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.sunway.course.timetable.view.MainApp;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

@Component
public class MainPageController {

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

    private final MainApp mainApp;

    public MainPageController(@Lazy MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void initialize(){
        title.setText("SCTS");
        subheading.setText("Dashboard");
        homeButton.setText("homeButton");
        generateTimetable.setText("Generate Timetable");
        viewTimetable.setText("View Timetable");
        logOutButton.setText("Log Out");

        setButtonHoverEffect(homeButton);
        setButtonHoverEffect(generateTimetable);
        setButtonHoverEffect(viewTimetable);
        setButtonHoverEffect(logOutButton);
    }

    @FXML
    private void logOut() {
        try {
            // Get the current stage (window) and close it
            Stage stage = (Stage) logOutButton.getScene().getWindow();
            stage.close();

            mainApp.loadLoginPage(stage);  // Handle exception properly
        } catch (Exception e) {
            e.printStackTrace(); // Print the error if something goes wrong
        }
    }

    private void setButtonHoverEffect(Button button) {
        button.setOnMouseEntered(e -> button.setCursor(Cursor.HAND));
        button.setOnMouseExited(e -> button.setCursor(Cursor.DEFAULT));
    }

    

}
