package com.sunway.course.timetable.controller.app;

import org.springframework.stereotype.Component;

import com.sunway.course.timetable.view.MainApp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Region;

@Component
public class ProgrammeController extends BaseController {

    @FXML
    private Label subheading, programmelabel, intake, year;
    
    @FXML
    private RadioButton programme, module, lecturer;

    @FXML
    private Region spacer1, spacer2, spacer3;

    public ProgrammeController(MainApp mainApp) {
        super(mainApp);
    }

    @Override
    protected void initialize() {
        super.initialize(); // Call BaseController's initialize()

        subheading.setText("View Programme");

        programme.setText("Programme");
        module.setText("Module");
        lecturer.setText("Lecturer");
        programmelabel.setText("Programme");
        intake.setText("Intake");
        year.setText("Year");

        // Handle RadioButton Selection
        ToggleGroup toggleGroup = new ToggleGroup();
        programme.setToggleGroup(toggleGroup);
        module.setToggleGroup(toggleGroup);
        lecturer.setToggleGroup(toggleGroup);

        programme.setOnAction(this::handleRadioSelection);
        module.setOnAction(this::handleRadioSelection);
        lecturer.setOnAction(this::handleRadioSelection);
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
        }
    }

}
