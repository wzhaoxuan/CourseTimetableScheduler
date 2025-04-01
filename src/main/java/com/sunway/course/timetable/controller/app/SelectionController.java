package com.sunway.course.timetable.controller.app;

import com.sunway.course.timetable.view.MainApp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

public abstract class SelectionController extends ContentController {

    @FXML
    protected Label subheading;

    @FXML
    protected RadioButton programme, module, lecturer;

    public SelectionController(MainApp mainApp) {
        super(mainApp);
    }

    @Override
    protected void initialize() {
        super.initialize(); // Call BaseController's initialize()
        programme.setText("Programme");
        module.setText("Module");
        lecturer.setText("Lecturer");

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
    protected void handleRadioSelection(ActionEvent event) {
        RadioButton selectedRadio = (RadioButton) event.getSource();
        System.out.println("Selected Type: " + selectedRadio.getText());

        if (programme.isSelected()) {
            loadPage("Programme");
        } else if (lecturer.isSelected()) {
            loadPage("Lecturer");
        } else if (module.isSelected()) {
            loadPage("Module");
        }
    }

    protected void loadPage(String pageName) {
        try {
            switch (pageName) {
                case "Programme":
                    MainApp.getInstance().loadProgrammePage();
                    break;
                case "Lecturer":
                    MainApp.getInstance().loadLecturerPage();
                    break;
                case "Module":
                    MainApp.getInstance().loadModulePage();
                    break;
                default:
                    System.out.println("Invalid page name: " + pageName);
            }
        } catch (Exception e) {
            e.printStackTrace();  // Log the error properly
        }
    }
}
