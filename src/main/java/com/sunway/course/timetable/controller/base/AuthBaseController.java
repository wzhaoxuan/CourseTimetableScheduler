package com.sunway.course.timetable.controller.base;
import com.sunway.course.timetable.service.NavigationService;
import com.sunway.course.timetable.view.MainApp;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public abstract class AuthBaseController extends BaseController{

    protected String username;
    protected String password;
    private final String titleName = "SunwayCTS";
    private final String icon = "/images/sunwaycts.png";

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button signUpButton;

    @FXML 
    private Label description;

    @FXML
    private ImageView logo;

    protected NavigationService navigationService;

    public AuthBaseController(NavigationService navigationService) {
        super(navigationService.getMainApp()); // set mainApp in BaseController for backward compatibility
        this.navigationService = navigationService;
    }

    @Override
    protected void initialize(){
        super.initialize(); // Call BaseController's initialize()
        title.setText(titleName);
        description.setText("Empowering smarter\nscheduling with\nAI precision");
        signUpButton.setText("Sign Up");
        signUpButton.setDefaultButton(true);

        usernameField.setPromptText("UserID");
        passwordField.setPromptText("Password");
        logo.setImage(new Image(getClass().getResourceAsStream(icon)));
    }
    
    @FXML
    protected abstract void signUp();

    protected void navigateToPage(String page) {
        try {
            switch(page){
                case "MainPage":
                    navigationService.loadMainPage();
                    break;
                case "LoginPage":
                    navigationService.loadLoginPage();
                    break;
                case "SignUpPage":
                    navigationService.loadSignUpPage();
                    break;
                default:
                    throw new IllegalArgumentException("Invalid page: " + page);
            }
        } catch (Exception e) {
            e.printStackTrace(); // Handle exception properly
        }
    }

    protected String trimUsername() {
        return usernameField.getText().trim();
    }

    protected String trimPassword() {
        return passwordField.getText().trim();
    }

}
