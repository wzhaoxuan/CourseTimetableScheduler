package com.sunway.course.timetable.controller.base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sunway.course.timetable.view.MainApp;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public abstract class BaseController {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @FXML protected Label title;

    protected final MainApp mainApp;

    public BaseController(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    protected void initialize() {
        // Initialize common components here if needed
    }
}
