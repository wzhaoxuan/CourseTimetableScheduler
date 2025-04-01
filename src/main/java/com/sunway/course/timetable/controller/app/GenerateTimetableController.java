package com.sunway.course.timetable.controller.app;

import org.springframework.stereotype.Component;

import com.sunway.course.timetable.controller.base.ContentController;
import com.sunway.course.timetable.view.MainApp;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

@Component
public class GenerateTimetableController extends ContentController {

    @FXML
    private Label subheading, programme, year, intake, semester, venue;

    @FXML
    private ChoiceBox<String> programmeChoice, yearChoice, intakeChoice, semesterChoice;

    @FXML
    private TextField venueField;

    @FXML
    private Button generateButton;

    @FXML
    private Region spacer1, spacer2, spacer3;

    public GenerateTimetableController(MainApp mainApp) {
        super(mainApp);
    }

    @Override
    protected void initialize() {
        super.initialize(); // Call BaseController's initialize()

        subheading.setText("Generate Timetable");

        // Make the spacers expand
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        HBox.setHgrow(spacer3, Priority.ALWAYS);

        programme.setText("Programme:");
        year.setText("Year:");
        intake.setText("Intake:");
        semester.setText("Semester:");
        venue.setText("Venue:");
        generateButton.setText("Generate");

        venueField.setPromptText("UW 2-5");

        programmeChoice.getItems().addAll("Diploma in IT", "Diploma in Business", "Diploma in Communication");
        yearChoice.getItems().addAll("2022", "2023", "2024");
        intakeChoice.getItems().addAll("January", "April", "August");
        semesterChoice.getItems().addAll("1", "2", "3");

        setButtonHoverEffect(generateButton);
    }
}
