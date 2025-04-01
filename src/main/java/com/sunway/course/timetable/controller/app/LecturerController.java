package com.sunway.course.timetable.controller.app;

import org.springframework.stereotype.Component;

import com.sunway.course.timetable.view.MainApp;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

@Component
public class LecturerController extends SelectionController{
    
    @FXML
    private RadioButton full_time, part_time, teaching_assistant;


    public LecturerController(MainApp mainApp) {
        super(mainApp);
    }

    @Override
    protected void initialize() {
        super.initialize(); // Call BaseController's initialize()

        subheading.setText("View Lecturer");

        full_time.setText("FullTime");
        part_time.setText("PartTime");
        teaching_assistant.setText("TeachingAssistant");

        // Handle RadioButton Selection
        ToggleGroup toggleGroup = new ToggleGroup();

        full_time.setToggleGroup(toggleGroup);
        part_time.setToggleGroup(toggleGroup);
        teaching_assistant.setToggleGroup(toggleGroup);

        full_time.setOnAction(this::handleRadioSelection);
        part_time.setOnAction(this::handleRadioSelection);
        teaching_assistant.setOnAction(this::handleRadioSelection);
    }
}
