package com.sunway.course.timetable.controller.app;

import org.springframework.stereotype.Component;

import com.sunway.course.timetable.controller.base.ContentController;
import com.sunway.course.timetable.view.MainApp;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
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
    private GridPane venueGrid;

    @FXML
    private Region spacer1, spacer2, spacer3, spacer4;

    private int currentRow = 0; // Track the next available row in the grid
    private int currentCol = 0; // Track the next available column in the grid
    private final int maxColumns = 8; // Maximum number of columns in the grid

    public GenerateTimetableController(MainApp mainApp) {
        super(mainApp);
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

        setButtonHoverEffect(generateButton);
        setButtonHoverEffect(sectionButton);
    }

    private void addVenueToGrid(String venueName){
        if (venueName == null || venueName.isEmpty()) {
            return; // Ignore empty venue names
        }

        Label venueLabel = new Label(venueName);
        GridPane.setHgrow(venueLabel, Priority.ALWAYS);
        GridPane.setVgrow(venueLabel, Priority.ALWAYS);

        venueGrid.add(venueLabel, currentCol, currentRow); // Add the venue label to the grid
        
        currentCol++; // Increment the column for the next venue
        if (currentCol >= maxColumns) { // If the row is full, move to the next row
            currentCol = 0;
            currentRow++;
        }
    }
}
