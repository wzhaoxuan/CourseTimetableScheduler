package com.sunway.course.timetable.controller.app;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class MainPageController {

    @FXML
    private Label username;
    
    @FXML
    private Label title;

    @FXML
    private Label subheading;

    @FXML
    private Button home;

    @FXML
    private Button generateTimetable;

    @FXML
    private Button viewTimetable;

    @FXML
    private Button logOut;

    @FXML
    private void initialize(){
        title.setText("SCTS");
        subheading.setText("Dashboard");
        home.setText("Home");
        generateTimetable.setText("Generate Timetable");
        viewTimetable.setText("View Timetable");
        logOut.setText("Log Out");
    }

}
