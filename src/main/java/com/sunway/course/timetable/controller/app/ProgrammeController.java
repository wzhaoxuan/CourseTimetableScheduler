package com.sunway.course.timetable.controller.app;

import org.springframework.stereotype.Component;

import com.sunway.course.timetable.controller.authentication.LoginSceneController;
import com.sunway.course.timetable.controller.base.SelectionController;
import com.sunway.course.timetable.service.NavigationService;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

@Component
public class ProgrammeController extends SelectionController{

    @FXML
    private Label programmelabel, intake, year;

    public ProgrammeController(NavigationService navService, LoginSceneController loginController){
        super(navService, loginController);
    }

    @Override
    protected void initialize() {
        super.initialize(); // Call BaseController's initialize()
        subheading.setText("View Programme");

        programmelabel.setText("Programme");
        intake.setText("Intake");
        year.setText("Year");
    }

}
