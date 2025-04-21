package com.sunway.course.timetable.view;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import com.sunway.course.timetable.CourseTimetableSchedularApplication;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

@Component // Spring-managed bean
// @Lazy //Prevents early initiallization before JavaFX is ready
public class MainApp extends Application {
    private static ConfigurableApplicationContext springContext;
    private static MainApp instance; // Singleton reference
    private Stage primaryStage;
    private final String title = "SunwayCTS";
    private final String icon = "/images/sunwaycts.png";

    @Override
    public void init() throws Exception {
        // Start Spring Boot in the JavaFX lifecycle
        springContext = new SpringApplicationBuilder(CourseTimetableSchedularApplication.class).run();
        instance = this;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        primaryStage.setTitle(title);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream(icon)));
        loadLoginPage();
        
    }

    public void loadLoginPage() throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/desktop/course/timetable/LoginScene.fxml"));
        // Use Spring dependency injection
        fxmlLoader.setControllerFactory(springContext::getBean);

        
        Scene scene = new Scene(fxmlLoader.load());

        // Ensure scene resizes with window
        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> scene.getRoot().resize(newVal.doubleValue(), primaryStage.getHeight()));
        primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> scene.getRoot().resize(primaryStage.getWidth(), newVal.doubleValue()));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void loadSignUpPage() throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/desktop/course/timetable/SignUpScene.fxml"));
        // Use Spring dependency injection
        fxmlLoader.setControllerFactory(springContext::getBean);

        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void loadMainPage() throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/desktop/course/timetable/MainPageScene.fxml"));
        // Use Spring dependency injection
        fxmlLoader.setControllerFactory(springContext::getBean);  

        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void loadGenerateTimetablePage() throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/desktop/course/timetable/GenerateTimetableScene.fxml"));
        // Use Spring dependency injection
        fxmlLoader.setControllerFactory(springContext::getBean);

        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void loadLecturerAvailabilityPage() throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/desktop/course/timetable/LecturerAvailabilityScene.fxml"));
        // Use Spring dependency injection
        fxmlLoader.setControllerFactory(springContext::getBean);

        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void loadViewTimetablePage() throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/desktop/course/timetable/ViewTimetableScene.fxml"));
        // Use Spring dependency injection
        fxmlLoader.setControllerFactory(springContext::getBean);

        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void loadProgrammePage() throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/desktop/course/timetable/ProgrammeScene.fxml"));
        // Use Spring dependency injection
        fxmlLoader.setControllerFactory(springContext::getBean);

        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void loadLecturerPage() throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/desktop/course/timetable/LecturerScene.fxml"));
        // Use Spring dependency injection
        fxmlLoader.setControllerFactory(springContext::getBean);

        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void loadModulePage() throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/desktop/course/timetable/ModuleScene.fxml"));
        // Use Spring dependency injection
        fxmlLoader.setControllerFactory(springContext::getBean);

        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static MainApp getInstance() {
        return instance;
    }

    public String getIcon() {
        return icon;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public void stop(){
        if(springContext != null){
            springContext.close(); // Ensure Spring context is closed when JavaFX stops
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
