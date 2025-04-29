package com.sunway.course.timetable.controller.app;

import javafx.application.Platform;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.sunway.course.timetable.model.Lecturer;
import com.sunway.course.timetable.controller.authentication.LoginSceneController;
import com.sunway.course.timetable.controller.base.ContentController;
import com.sunway.course.timetable.event.LecturerConstraintConfirmedEvent;
import com.sunway.course.timetable.service.NavigationService;
import com.sunway.course.timetable.util.DynamicGridManager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

@Component
public class GenerateTimetableController extends ContentController {

    @FXML
    private Label programme, year, intake, semester, venue, lecturerAvailable;

    @FXML
    private ChoiceBox<String> programmeChoice, yearChoice, intakeChoice, semesterChoice;

    @FXML
    private TextField venueField;

    @FXML
    private Button generateButton, sectionButton;

    @FXML
    private GridPane venueGrid, weekdayGrid;

    @FXML
    private ScrollPane venueScroll, weekdayScroll;

    @FXML
    private Region spacer1, spacer2, spacer3, spacer4;

    private final int MAXCOLUMNS = 10;
    private final int MAXROWS = 10; 

    private DynamicGridManager venueGridManager;
    private DynamicGridManager weekdayGridManager;

    public GenerateTimetableController(NavigationService navService, 
                                        LoginSceneController loginController) {
        super(navService, loginController);
    }

    @Override
    protected void initialize() {
        super.initialize(); // Call BaseController's initialize()
        subheading.setText("Generate Timetable");
        lecturerAvailable.setText("Lecturer Available:");
        sectionButton.setText("Add Section");

        // Make the spacers expand
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        HBox.setHgrow(spacer3, Priority.ALWAYS);
        HBox.setHgrow(spacer4, Priority.ALWAYS);

        programme.setText("Programme:");
        year.setText("Year:");
        intake.setText("Intake:");
        semester.setText("Semester:");
        venue.setText("Venue:");
        generateButton.setText("Generate");

        venueField.setPromptText("UW 2-5");
        venueField.setOnAction(event -> {
            String venue = venueField.getText().trim();
            addVenueToGrid(venue); // Add the venue to the grid
            venueField.clear(); // Clear the field after adding
        });

        programmeChoice.getItems().addAll("Diploma in IT", "Diploma in Business", "Diploma in Communication");
        yearChoice.getItems().addAll("2022", "2023", "2024");
        intakeChoice.getItems().addAll("January", "April", "August");
        semesterChoice.getItems().addAll("1", "2", "3");

        venueGridManager = new DynamicGridManager(venueGrid, MAXCOLUMNS, MAXROWS);
        weekdayGridManager = new DynamicGridManager(weekdayGrid, MAXCOLUMNS, MAXROWS);
    }

    @FXML
    private void addSection(){
        try {
            navigationService.loadLecturerAvailabilityPage(); // Handle exception properly
        } catch (Exception e) {
            e.printStackTrace(); // Print the error if something goes wrong
        }
    }

    @EventListener
    public void handleLecturerConstraintConfirmed(LecturerConstraintConfirmedEvent event) {
        Lecturer lecturer = event.getLecturer();
        String lecturerName = lecturer.getName();
        Long lecturerId = lecturer.getId();
        Platform.runLater(()-> {
            addWeekDayConstraintToGrid(lecturerName, lecturerId);
        });
    }

    private void addVenueToGrid(String venueName){
        venueGridManager.addButton(venueName, "venue-button");
    }

    private void addWeekDayConstraintToGrid(String lecturerName, Long lecturerId) {
        weekdayGridManager.addButton(lecturerName, "lecturer-button");
        System.out.println("Added Weekday Constraint: " + lecturerName);
    }

}
