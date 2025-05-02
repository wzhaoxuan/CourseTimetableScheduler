package com.sunway.course.timetable.controller.base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunway.course.timetable.controller.authentication.LoginSceneController;
import com.sunway.course.timetable.service.NavigationService;
import com.sunway.course.timetable.view.MainApp;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public abstract class ContentController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ContentController.class);

    @FXML protected ImageView profile;
    @FXML protected Label username, subheading;
    @FXML protected Button homeButton, generateTimetable, viewTimetable, logOutButton;

    protected NavigationService navigationService;
    private final LoginSceneController loginSceneController;

    @Autowired
    public ContentController(NavigationService navigationService, 
                                LoginSceneController loginSceneController) {
        super(navigationService.getMainApp());
        this.navigationService = navigationService;
        this.loginSceneController = loginSceneController;
    }

    @Override
    protected void initialize() {
        title.setText("SCTS");
        setupButtonText();
        setUsernameLabel();
    }

    public void setUsernameLabel(){
        String uppercase_username = loginSceneController.getUsername();
        this.username.setText(capitalize(uppercase_username));
    }

    public String capitalize(String username) {
        if (username == null || username.isEmpty()) {
            return username;
        }
        return username.substring(0, 1).toUpperCase() + username.substring(1);
    }

    public void setupButtonText(){
        homeButton.setText("Home");
        generateTimetable.setText("Generate Timetable");
        viewTimetable.setText("View Timetable");
        logOutButton.setText("Log Out");
    }

    @FXML
    protected void home() {
        try {
            navigationService.loadMainPage();
        } catch (Exception e) {
            logger.error("Failed to load Main Page", e);
        }
    }

    @FXML
    protected void generateTimetable() {
        try {
            navigationService.loadGenerateTimetablePage();
        } catch (Exception e) {
            logger.error("Failed to load Generate Timetable Page", e);
        }
    }

    @FXML
    protected void viewTimetable() {
        try {
            navigationService.loadViewTimetablePage();
        } catch (Exception e) {
            logger.error("Failed to load View Timetable Page", e);
        }
    }

    @FXML
    protected void logOut() {
        try {
            navigationService.loadLoginPage();
        } catch (Exception e) {
            logger.error("Failed to load Login Page", e);
        }
    }
}
