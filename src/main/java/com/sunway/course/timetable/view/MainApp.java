package com.sunway.course.timetable.view;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import com.sunway.course.timetable.CourseTimetableSchedularApplication;
import com.sunway.course.timetable.config.ControllerFactory;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {
    private ConfigurableApplicationContext context;
    private Stage primaryStage;
    private String title = "SunwayCTS";
    private String icon = "/images/sunwaycts.png";

    @Override
    public void init() throws Exception {
        // Start Spring Boot in the JavaFX lifecycle
        context = new SpringApplicationBuilder(CourseTimetableSchedularApplication.class).run();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        loadLoginPage();
        
    }

    public void loadLoginPage() throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/desktop/course/timetable/LoginScene.fxml"));
        // Use ControllerFactory for dependency injection
        fxmlLoader.setControllerFactory(new ControllerFactory(this));
        
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setTitle(title);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream(icon)));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void loadMainPage() throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/desktop/course/timetable/MainPageScene.fxml"));
        fxmlLoader.setControllerFactory(new ControllerFactory(this));  // Use the factory

        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.getIcons().add(new Image(getClass().getResourceAsStream(icon)));
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        context.close(); // Ensure Spring context is closed when JavaFX stops
    }

    public static void main(String[] args) {
        launch(args);
    }
}
