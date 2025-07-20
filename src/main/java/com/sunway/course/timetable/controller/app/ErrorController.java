package com.sunway.course.timetable.controller.app;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sunway.course.timetable.service.NavigationService;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

@Component
public class ErrorController {

    private static final Logger log = LoggerFactory.getLogger(ErrorController.class);
    private NavigationService navigationService;

    @FXML private Label errorLabel, errorHeading;
    @FXML private Button backButton;
    
    ErrorController(NavigationService navigationService) {
        this.navigationService = navigationService;
    }

    public void initialize() {
        errorHeading.setText("Error");
        backButton.setText("Back");
    }
    /** Called by the loader to pass in the exception text. */
    public void setErrorMessage(String msg) {
        errorLabel.setText(msg);
    }

    /** Invoked when the user clicks “Back”. */
    @FXML
    private void onBack() throws IOException {
        try {
            navigationService.loadGenerateTimetablePage();
        } catch (Exception e) {
            log.error("Failed to load View Timetable Page", e);
        }
    }

}
