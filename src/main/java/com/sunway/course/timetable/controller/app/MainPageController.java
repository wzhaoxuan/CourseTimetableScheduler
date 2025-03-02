package com.sunway.course.timetable.controller.app;

import org.springframework.stereotype.Component;

import com.sunway.course.timetable.view.MainApp;

import javafx.fxml.FXML;
import javafx.scene.control.Label;


@Component
public class MainPageController extends BaseController {

    @FXML
    private Label subheading;

    public MainPageController(MainApp mainApp) {
        super(mainApp);
    }

    @Override
    protected void initialize(){
        super.initialize(); // Call BaseController's initialize()
        subheading.setText("Dashboard");
    }

}
