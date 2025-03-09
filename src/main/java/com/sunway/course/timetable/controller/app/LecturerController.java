package com.sunway.course.timetable.controller.app;

import org.springframework.stereotype.Component;

import com.sunway.course.timetable.view.MainApp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

@Component
public class LecturerController extends BaseController {
    
    @FXML
    private Label subheading;
    
    @FXML
    private RadioButton programme, module, lecturer, full_time, part_time, teaching_assistant;


    public LecturerController(MainApp mainApp) {
        super(mainApp);
    }

    @Override
    protected void initialize() {
        super.initialize(); // Call BaseController's initialize()

        subheading.setText("View Lecturer");

        programme.setText("Programme");
        module.setText("Module");
        lecturer.setText("Lecturer");
        full_time.setText("FullTime");
        part_time.setText("PartTime");
        teaching_assistant.setText("TeachingAssistant");

        // Handle RadioButton Selection
        ToggleGroup toggleGroup = new ToggleGroup();
        programme.setToggleGroup(toggleGroup);
        module.setToggleGroup(toggleGroup);
        lecturer.setToggleGroup(toggleGroup);
        full_time.setToggleGroup(toggleGroup);
        part_time.setToggleGroup(toggleGroup);
        teaching_assistant.setToggleGroup(toggleGroup);

        programme.setOnAction(this::handleRadioSelection);
        module.setOnAction(this::handleRadioSelection);
        lecturer.setOnAction(this::handleRadioSelection);
        full_time.setOnAction(this::handleRadioSelection);
        part_time.setOnAction(this::handleRadioSelection);
        teaching_assistant.setOnAction(this::handleRadioSelection);
    }

    @FXML
    private void handleRadioSelection(ActionEvent event) {
        RadioButton selectedRadio = (RadioButton) event.getSource();
        System.out.println("Selected Type: " + selectedRadio.getText());
        if(programme.isSelected()){
            try {
                MainApp.getInstance().loadProgrammePage(); // Handle exception properly
            } catch (Exception e) {
                e.printStackTrace(); // Print the error if something goes wrong
            }
        } else if (lecturer.isSelected()){
            try {
                MainApp.getInstance().loadLecturerPage(); // Handle exception properly
            } catch (Exception e) {
                e.printStackTrace(); // Print the error if something goes wrong
            }
        } else if (module.isSelected()){
            try {
                MainApp.getInstance().loadModulePage(); // Handle exception properly
            } catch (Exception e) {
                e.printStackTrace(); // Print the error if something goes wrong
            }
        }
    }
}
