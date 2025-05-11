package com.sunway.course.timetable.controller.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunway.course.timetable.interfaces.services.WeekDayConstraintService;
import com.sunway.course.timetable.service.NavigationService;

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

    @FXML private Label subheading, id, unavaliableDay;
    @FXML private TextField lecturerId;
    @FXML private Button confirm;
    @FXML private CheckBox monday, tuesday, wednesday, thursday, friday;
    @FXML private Region spacer1, spacer2, spacer3, spacer4;

    private final WeekDayConstraintService weekDayConstraintService;
    private final NavigationService navigationService;

    @Autowired
    public LecturerAvailabilityController(NavigationService navigationService,
                                          WeekDayConstraintService weekDayConstraintService) {
        this.navigationService = navigationService;
        this.weekDayConstraintService = weekDayConstraintService;
    }

    public void initialize() {
        setupLabelText();
        // Make the spacers expand
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        HBox.setHgrow(spacer3, Priority.ALWAYS);
        HBox.setHgrow(spacer4, Priority.ALWAYS);
    }

    private void setupLabelText(){
        subheading.setText("Lecturer Availability");
        id.setText("ID: ");
        unavaliableDay.setText("Unavailable Day");
        monday.setText("Monday");
        tuesday.setText("Tuesday");
        wednesday.setText("Wednesday");
        thursday.setText("Thursday");
        friday.setText("Friday");
        confirm.setText("Confirm");
        lecturerId.setPromptText("21033105");
    }

    @FXML
    private void handleConfirmButton() {
        String lecturerIdText  = lecturerId.getText().trim(); // Get the ID from the text field

        if (lecturerIdText.isEmpty()) {
            System.out.println("Lecturer ID is required.");
        } else {
            try {
                weekDayConstraintService.selectWeedayConstraint(lecturerIdText, monday, tuesday, wednesday, thursday, friday); 
                navigationService.loadGenerateTimetablePage();
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}
