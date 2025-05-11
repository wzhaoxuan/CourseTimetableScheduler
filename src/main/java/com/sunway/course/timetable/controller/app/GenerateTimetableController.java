package com.sunway.course.timetable.controller.app;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.sunway.course.timetable.controller.authentication.LoginSceneController;
import com.sunway.course.timetable.controller.base.ContentController;
import com.sunway.course.timetable.event.LecturerConstraintConfirmedEvent;
import com.sunway.course.timetable.event.VenueAddedEvent;
import com.sunway.course.timetable.interfaces.services.VenueService;
import com.sunway.course.timetable.service.NavigationService;
import com.sunway.course.timetable.store.VenueSessionStore;
import com.sunway.course.timetable.store.WeekdaySessionStore;
import com.sunway.course.timetable.util.date.DateUtils;
import com.sunway.course.timetable.util.grid.DynamicGridManager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

@Component
public class GenerateTimetableController extends ContentController {

    @FXML private Label programme, year, intake, semester, venue, lecturerAvailable;
    @FXML private ComboBox<String>  programmeChoice, yearChoice, intakeChoice, semesterChoice;
    @FXML private TextField venueField;
    @FXML private Button generateButton, sectionButton;
    @FXML private GridPane venueGrid, weekdayGrid;
    @FXML private ScrollPane venueScroll, weekdayScroll;
    @FXML private Region spacer1, spacer2, spacer3, spacer4;

    private final int MAXCOLUMNS = 10;
    private final int MAXROWS = 10;

    private DynamicGridManager venueGridManager;
    private DynamicGridManager weekdayGridManager;

    private final VenueService venueService;
    private final ApplicationEventPublisher eventPublisher;
    private final VenueSessionStore venueStore;
    private final WeekdaySessionStore weekdayStore;

    public GenerateTimetableController(NavigationService navService, 
                                        LoginSceneController loginController,
                                        VenueService venueService,
                                        ApplicationEventPublisher eventPublisher,
                                        VenueSessionStore venueSessionStore,
                                        WeekdaySessionStore weekdaySessionStore
                                        ) {
        super(navService, loginController);
        this.venueService = venueService;
        this.eventPublisher = eventPublisher;
        this.venueStore = venueSessionStore;
        this.weekdayStore = weekdaySessionStore;
    }

    @Override
    public void initialize() {
        super.initialize();
        setupLabelsText();
        setupComboBoxes();
        setupLayout();
        setupVenueField();

        venueGridManager = createVenueGridManager();
        weekdayGridManager = createWeekdayGridManager();


        venueStore.get().forEach(this::addVenueToGrid);
        weekdayStore.get().forEach(lecturer -> addWeekDayConstraintToGrid(lecturer, null)); // Adjust if ID is needed
    }

    @FXML
    private void addSection(){
        try {
            navigationService.loadLecturerAvailabilityPage(); // Handle exception properly
        } catch (Exception e) {
            e.printStackTrace(); // Print the error if something goes wrong
        }
    }

    @FXML
    private void generate() {
        try{
            navigationService.loadTimetablePage(); // Handle exception properly
        } catch (Exception e) {
            e.printStackTrace(); // Print the error if something goes wrong
        }
    }

    @EventListener
    public void handleLecturerConstraintConfirmed(LecturerConstraintConfirmedEvent event) {
        String lecturerName = event.getLecturer().getName();
        Long lecturerId = event.getLecturer().getId();
        if (weekdayStore.add(lecturerName)) {
            addWeekDayConstraintToGrid(lecturerName, lecturerId);
        }
    }

    @EventListener
    public void handleVenueAdded(VenueAddedEvent event) {
        String venueName = event.getVenue().getName();
        if (venueStore.add(venueName)) {
            addVenueToGrid(venueName);
        }
    }

    private void addVenueToGrid(String venueName){
        venueGridManager.addButton(venueName, "venue-button");
    }

    private void addWeekDayConstraintToGrid(String lecturerName, Long lecturerId) {
        weekdayGridManager.addButton(lecturerName, "lecturer-button");
        System.out.println("Added Weekday Constraint: " + lecturerName);
    }

    private void setupLabelsText() {
        subheading.setText("Generate Timetable");
        programme.setText("Programme:");
        year.setText("Year:");
        intake.setText("Intake:");
        semester.setText("Semester:");
        venue.setText("Venue:");
        lecturerAvailable.setText("Lecturer Available:");
        venueField.setPromptText("UW 2-5");
        generateButton.setText("Generate");
        sectionButton.setText("Add Section");
    }

    private void setupComboBoxes() {
        programmeChoice.getItems().addAll("Diploma in IT", "Diploma in Business", "Diploma in Communication");
        programmeChoice.setVisibleRowCount(5);
        yearChoice.getItems().addAll(DateUtils.getYearOptions());
        yearChoice.setVisibleRowCount(5);
        intakeChoice.getItems().addAll(DateUtils.getMonths());
        intakeChoice.setVisibleRowCount(5);
        semesterChoice.getItems().addAll("1", "2", "3", "4", "5", "6", "7", "8", "9");
        semesterChoice.setVisibleRowCount(5);
    }

    private void setupLayout() {
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        HBox.setHgrow(spacer3, Priority.ALWAYS);
        HBox.setHgrow(spacer4, Priority.ALWAYS);
    }

    private void setupVenueField() {
        venueField.setOnAction(event -> {
            String venue = venueField.getText().trim();
            if (!venue.isEmpty()) {
                venueService.publishVenueAddedEvent(venue);
                venueField.clear();
            }
        });
    }

    protected DynamicGridManager createVenueGridManager() {
        return new DynamicGridManager(venueGrid, MAXCOLUMNS, MAXROWS);
    }
    
    protected DynamicGridManager createWeekdayGridManager() {
        return new DynamicGridManager(weekdayGrid, MAXCOLUMNS, MAXROWS);
    }
    
}
