package com.sunway.course.timetable.controller.app;
import org.springframework.stereotype.Component;

import com.sunway.course.timetable.view.MainApp;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

@Component
public class LecturerAvailabilityController{

    @FXML
    private Label subheading, id, unavaliableDay;

    @FXML
    private TextField lecturerId;

    @FXML 
    private Button confirm;

    @FXML
    private CheckBox monday, tuesday, wednesday, thursday, friday;

    @FXML
    private Region spacer1, spacer2, spacer3, spacer4;

    public final MainApp mainApp;

    public LecturerAvailabilityController(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public void initialize() {
        subheading.setText("Lecturer Availability");
        id.setText("ID");
        unavaliableDay.setText("Unavailable Day");
        monday.setText("Monday");
        tuesday.setText("Tuesday");
        wednesday.setText("Wednesday");
        thursday.setText("Thursday");
        friday.setText("Friday");
        confirm.setText("Confirm");

        lecturerId.setPromptText("21033105");

        // Make the spacers expand
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        HBox.setHgrow(spacer3, Priority.ALWAYS);
        HBox.setHgrow(spacer4, Priority.ALWAYS);
    }

    @FXML
    private void handleConfirmButton() {
        // Handle the confirm button action here
        // String lecturerIdText = lecturerId.getText();
        // boolean isMondayUnavailable = monday.isSelected();
        // boolean isTuesdayUnavailable = tuesday.isSelected();
        // boolean isWednesdayUnavailable = wednesday.isSelected();
        // boolean isThursdayUnavailable = thursday.isSelected();
        // boolean isFridayUnavailable = friday.isSelected();

        // // Perform your logic with the selected values
        // System.out.println("Lecturer ID: " + lecturerIdText);
        // System.out.println("Monday Unavailable: " + isMondayUnavailable);
        // System.out.println("Tuesday Unavailable: " + isTuesdayUnavailable);
        // System.out.println("Wednesday Unavailable: " + isWednesdayUnavailable);
        // System.out.println("Thursday Unavailable: " + isThursdayUnavailable);
        // System.out.println("Friday Unavailable: " + isFridayUnavailable);

        try {
            MainApp.getInstance().loadGenerateTimetablePage(); // Handle exception properly
        } catch (Exception e) {
            e.printStackTrace(); // Print the error if something goes wrong
        }
    }
}
