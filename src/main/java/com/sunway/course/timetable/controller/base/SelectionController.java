package com.sunway.course.timetable.controller.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunway.course.timetable.controller.authentication.LoginSceneController;
import com.sunway.course.timetable.service.NavigationService;
import com.sunway.course.timetable.view.MainApp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

public abstract class SelectionController extends ContentController {

    private static final Logger logger = LoggerFactory.getLogger(SelectionController.class);

    @FXML protected Label subheading;
    @FXML protected RadioButton programme, module, lecturer;

    @Autowired
    public SelectionController(NavigationService navigationService, 
                                LoginSceneController loginSceneController) {
        super(navigationService, loginSceneController);
    }

    @Override
    protected void initialize() {
        super.initialize(); // Call BaseController's initialize()
         setupRadioButtons();
    }

    private void setupRadioButtons() {
        programme.setText("Programme");
        module.setText("Module");
        lecturer.setText("Lecturer");

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
        String selectedText = selectedRadio.getText();
        logger.info("Selected Type: {}", selectedText);

        try {
            switch (selectedText) {
                case "Programme":
                    navigationService.loadProgrammePage();
                    break;
                case "Lecturer":
                    navigationService.loadLecturerPage();
                    break;
                case "Module":
                    navigationService.loadModulePage();
                    break;
                default:
                    logger.warn("Invalid selection: {}", selectedText);
            }
        } catch (Exception e) {
            logger.error("Failed to load page for selection: {}", selectedText, e);
        }
    }
}
