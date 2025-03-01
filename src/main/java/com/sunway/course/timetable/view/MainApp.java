package com.sunway.course.timetable.view;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.sunway.course.timetable.CourseTimetableSchedularApplication;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

@Component // Spring-managed bean
@Lazy //Prevents early initiallization before JavaFX is ready
public class MainApp extends Application {
    private static ConfigurableApplicationContext springContext;
    private static MainApp instance; // Singleton reference
    private Stage primaryStage;
    private String title = "SunwayCTS";
    private String icon = "/images/sunwaycts.png";

    @Override
    public void init() throws Exception {
        // Start Spring Boot in the JavaFX lifecycle
        springContext = new SpringApplicationBuilder(CourseTimetableSchedularApplication.class).run();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        loadLoginPage(primaryStage);
        
    }

    public void loadLoginPage(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/desktop/course/timetable/LoginScene.fxml"));
        // Use Spring dependency injection
        fxmlLoader.setControllerFactory(springContext::getBean);
        
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle(title);
        stage.getIcons().add(new Image(getClass().getResourceAsStream(icon)));
        stage.setScene(scene);
        stage.show();
    }

    public void loadMainPage() throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/desktop/course/timetable/MainPageScene.fxml"));
        // Use Spring dependency injection
        fxmlLoader.setControllerFactory(springContext::getBean);  

        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.getIcons().add(new Image(getClass().getResourceAsStream(icon)));
        stage.setScene(scene);
        stage.show();
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
