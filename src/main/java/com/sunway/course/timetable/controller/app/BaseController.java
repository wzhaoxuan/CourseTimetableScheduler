package com.sunway.course.timetable.controller.app;

import com.sunway.course.timetable.view.MainApp;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public abstract class BaseController {

    @FXML
    protected Label title;

    protected final MainApp mainApp;

    public BaseController(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    protected void initialize() {
        // Initialize common components here if needed
    }

    protected void setButtonHoverEffect(Button button) {
        button.setOnMouseEntered(e -> button.setCursor(Cursor.HAND));
        button.setOnMouseExited(e -> button.setCursor(Cursor.DEFAULT));
    }

}
