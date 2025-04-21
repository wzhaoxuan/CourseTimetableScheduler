package com.sunway.course.timetable.controller.app;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunway.course.timetable.model.Lecturer;
import com.sunway.course.timetable.model.WeekDayConstraint;
import com.sunway.course.timetable.service.LecturerService;
import com.sunway.course.timetable.service.WeekDayConstraintService;
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

    @Autowired
    private LecturerService lecturerService; // Assuming you have a repository for fetching lecturers

    @Autowired
    private WeekDayConstraintService weekDayConstraintService; // Assuming you have a repository for saving constraints

    @Autowired
    private GenerateTimetableController generateTimetableController; // Assuming you have a controller for generating timetables

    @Autowired
    private MainApp mainApp;

    // public LecturerAvailabilityController(MainApp mainApp) {
    //     this.mainApp = mainApp;
    // }

    public void initialize() {
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

        // Make the spacers expand
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        HBox.setHgrow(spacer3, Priority.ALWAYS);
        HBox.setHgrow(spacer4, Priority.ALWAYS);
    }

    @FXML
    private void handleConfirmButton() {
        String lecturerIdText  = lecturerId.getText().trim(); // Get the ID from the text field

        if (lecturerIdText.isEmpty()) {
            System.out.println("Lecturer ID is required.");
            return; // Exit if the ID is empty
        }

        try {
            Long lecturerIdLong = Long.parseLong(lecturerIdText);
            Lecturer lecturer = findLecturerById(lecturerIdLong);
            if (lecturer == null) return;
    
            WeekDayConstraint constraint = getOrCreateConstraint(lecturerIdLong);
            updateConstraintWithUIValues(constraint, lecturer);
            saveConstraint(constraint);
    
            System.out.println("Availability saved for Lecturer ID: " + lecturerIdLong);

            //Add button to weekdayGrid in GenerateTimetableController
            generateTimetableController.addWeekDayConstraintToGrid(lecturer.getName());

            MainApp.getInstance().loadGenerateTimetablePage();

        } catch (NumberFormatException e) {
            System.out.println("Invalid Lecturer ID. Must be a number.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Lecturer findLecturerById(Long id) {
        Optional<Lecturer> lecturer = lecturerService.getLecturerById(id);

        if (lecturer.isEmpty()){
            System.out.println("Lecturer not found with ID: " + id);
            return null; // Exit if the lecturer is not found
        }

        return lecturer.get(); // Return the found lecturer
    }

    private WeekDayConstraint getOrCreateConstraint(Long lecturerId) {
        return weekDayConstraintService.getByLecturerId(lecturerId)
                .orElse(new WeekDayConstraint()); // Create a new constraint if not found
    }

    private void updateConstraintWithUIValues(WeekDayConstraint constraint, Lecturer lecturer) {
        constraint.setLecturer(lecturer); // Set the lecturer for the constraint
        constraint.setMonday(monday.isSelected());
        constraint.setTuesday(tuesday.isSelected());
        constraint.setWednesday(wednesday.isSelected());
        constraint.setThursday(thursday.isSelected());
        constraint.setFriday(friday.isSelected());
    }

    private void saveConstraint(WeekDayConstraint constraint) {
        weekDayConstraintService.addWeekDayConstraint(constraint); // Save the constraint to the database
    }
}
