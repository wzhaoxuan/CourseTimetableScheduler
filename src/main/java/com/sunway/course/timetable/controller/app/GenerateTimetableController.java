package com.sunway.course.timetable.controller.app;

import org.springframework.stereotype.Component;

import com.sunway.course.timetable.controller.authentication.LoginSceneController;
import com.sunway.course.timetable.controller.base.ContentController;
import com.sunway.course.timetable.service.NavigationService;
import com.sunway.course.timetable.view.MainApp;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;

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
    private ScrollPane scrollPane;

    @FXML
    private Region spacer1, spacer2, spacer3, spacer4;

    private int currentRow = 0; // Track the next available row in the grid
    private int currentCol = 0; // Track the next available column in the grid
    private final int MAXCOLUMNS = 10; // Maximum number of columns in the grid
    private final int MAXROWS = 10; // Maximum number of rows in the grid

    public GenerateTimetableController(NavigationService navService, LoginSceneController loginController) {
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
    }

    @FXML
    private void addSection(){
        try {
            MainApp.getInstance().loadLecturerAvailabilityPage(); // Handle exception properly
        } catch (Exception e) {
            e.printStackTrace(); // Print the error if something goes wrong
        }
    }

    private void addVenueToGrid(String venueName){
        if (venueName == null || venueName.isEmpty()) {
            return; // Ignore empty venue names
        }
        setupColumnGrid();

        Button venueButton = new Button(venueName);
        // Let it size to content — don't set max width/height unnecessarily
        venueButton.setMaxHeight(Region.USE_COMPUTED_SIZE); 
        venueButton.getStyleClass().add("venue-button"); 

        // Add the venue label to the grid
        venueGrid.add(venueButton, currentCol, currentRow); 
        rearrangeGrid();

        venueButton.setOnAction(e -> {
            // Remove the button from the grid
            venueGrid.getChildren().remove(venueButton);
            rearrangeGrid();
        });
    }

    public void addWeekDayConstraintToGrid(String lecturerName){
        if (lecturerName == null || lecturerName.isEmpty()) {
            return; // Ignore empty weekday names
        }

        Button weekDayButton = new Button(lecturerName);

        // Let it size to content — don't set max width/height unnecessarily
        weekDayButton.setMaxWidth(Region.USE_COMPUTED_SIZE); 
        weekDayButton.setMaxHeight(Region.USE_COMPUTED_SIZE); 
        weekDayButton.getStyleClass().add("lecturer-button"); 

        // Add the weekday label to the grid
        weekdayGrid.add(weekDayButton, currentCol, currentRow); 
        rearrangeGrid();

        weekDayButton.setOnAction(e -> {
            // Remove the button from the grid
            weekdayGrid.getChildren().remove(weekDayButton);
            rearrangeGrid();
        });

        System.out.println("Added Weekday Constraint: " + lecturerName);
    }

    private void setupColumnGrid(){
        if(venueGrid.getColumnConstraints().isEmpty()){
            for(int i = 0; i < MAXCOLUMNS; i++){
                ColumnConstraints column = new ColumnConstraints(); // Create a new column constraint
                column.setHgrow(Priority.ALWAYS); // Allow the column to grow
                column.setPercentWidth(100.0); // Set the width of the column to 10% of the grid width
                venueGrid.getColumnConstraints().add(column); // Add the column constraint to the grid
            }
        }

        if(venueGrid.getRowConstraints().isEmpty()){
            for(int i = 0; i < MAXROWS; i++){
                RowConstraints row = new RowConstraints(); // Create a new row constraint
                row.setVgrow(Priority.ALWAYS); // Allow the row to grow
                row.setPercentHeight(100.0); // Set the height of the row to 10% of the grid height
                venueGrid.getRowConstraints().add(row); // Add the row constraint to the grid
            }
        }
    }

    private void rearrangeGrid() {
        int col = 0;
        int row = 0;

        for(int i = 0; i < venueGrid.getChildren().size(); i++){
            Node venueIndex = venueGrid.getChildren().get(i);
            GridPane.setColumnIndex(venueIndex, col);
            GridPane.setRowIndex(venueIndex, row); // Set the new row index

            col++;
            if (col >= MAXCOLUMNS) { // If the row is full, move to the next row
                col = 0;
                row++;
            }
        }

        // Update the current row and column for the next venue
        currentCol = col;
        currentRow = row;
    }
}
