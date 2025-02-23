package com.sunway.course.timetable.view;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import com.sunway.course.timetable.CourseTimetableSchedularApplication;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {
    private ConfigurableApplicationContext context;

    @Override
    public void init() throws Exception {
        // Start Spring Boot in the JavaFX lifecycle
        context = new SpringApplicationBuilder(CourseTimetableSchedularApplication.class).run();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/desktop/course/timetable/LoginScene.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setTitle("SunwayCTS");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/sunwaycts.png")));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        context.close(); // Ensure Spring context is closed when JavaFX stops
    }

    public static void main(String[] args) {
        launch(args);
    }
}
