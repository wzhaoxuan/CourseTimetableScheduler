package com.sunway.course.timetable.controller.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sunway.course.timetable.controller.authentication.LoginSceneController;
import com.sunway.course.timetable.result.SelectionStateHolder;
import com.sunway.course.timetable.service.NavigationService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

public abstract class SelectionController extends ContentController {

    private static final Logger logger = LoggerFactory.getLogger(SelectionController.class);
    private final SelectionStateHolder stateHolder;

    @FXML protected Label subheading, type;
    @FXML protected RadioButton programme, module, lecturer;

    public SelectionController(NavigationService navigationService, 
                                LoginSceneController loginSceneController,
                                SelectionStateHolder stateHolder) {
        super(navigationService, loginSceneController);
        this.stateHolder = stateHolder;
    }

    @Override
    protected void initialize() {
        super.initialize(); // Call BaseController's initialize()
         setupRadioButtons();

         restorePreviousSelection();
    }

    private void setupRadioButtons() {
        type.setText("Type");
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

    private void restorePreviousSelection() {
        String previous = stateHolder.getSelectedType();
        if (previous == null) return;

        switch (previous) {
            case "Programme" -> programme.setSelected(true);
            case "Module" -> module.setSelected(true);
            case "Lecturer" -> lecturer.setSelected(true);
        }
    }

    @FXML
    protected void handleRadioSelection(ActionEvent event) {
        RadioButton selectedRadio = (RadioButton) event.getSource();
        String selectedText = selectedRadio.getText();
        stateHolder.setSelectedType(selectedText);  // Save current selection

        try {
            switch (selectedText) {
                case "Programme" -> navigationService.loadProgrammePage();
                case "Lecturer" -> navigationService.loadLecturerPage();
                case "Module" -> navigationService.loadModulePage();
                default -> logger.warn("Invalid selection: {}", selectedText);
            }
        } catch (Exception e) {
            logger.error("Failed to load page for selection: {}", selectedText, e);
        }
    }
}
