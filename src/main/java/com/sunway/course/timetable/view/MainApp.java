package com.sunway.course.timetable.view;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import com.sunway.course.timetable.CourseTimetableSchedularApplication;
import com.sunway.course.timetable.controller.app.ErrorController;
import com.sunway.course.timetable.service.NavigationService;

import akka.actor.typed.ActorSystem;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {
    private static ConfigurableApplicationContext springContext;
    private static MainApp instance; // Singleton reference
    public static ActorSystem<Void> actorSystem;
    public static HostServices hostServices;
    private Stage primaryStage;
    private final String title = "SunwayCTS";
    private final String icon = "/images/sunwaycts.png";

    @Override
    public void init() throws Exception {
        hostServices = getHostServices(); // Get HostServices for file opening
        // Start Spring Boot in the JavaFX lifecycle
        springContext = new SpringApplicationBuilder(CourseTimetableSchedularApplication.class).run();
        instance = this;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        primaryStage.setTitle(title);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream(icon)));

        NavigationService navigationService = springContext.getBean(NavigationService.class); 
        navigationService.setMainApp(this); // Set MainApp in NavigationService
        
        loadLoginPage();
    }

    public void loadLoginPage() throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/desktop/course/timetable/LoginScene.fxml"));
        // Use Spring dependency injection
        fxmlLoader.setControllerFactory(springContext::getBean);
        
        Scene scene = new Scene(fxmlLoader.load());
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

    public void loadTimetablePage() throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/desktop/course/timetable/TimetableScene.fxml"));
        // Use Spring dependency injection
        fxmlLoader.setControllerFactory(springContext::getBean);

        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void loadErrorPage(String errorMessage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/desktop/course/timetable/ErrorScene.fxml"));
        // Use Spring dependency injection
        fxmlLoader.setControllerFactory(springContext::getBean);

        Parent root = fxmlLoader.load();
        ErrorController errorController = fxmlLoader.getController();

        errorController.setErrorMessage(errorMessage); // Pass the error message to the controller
        
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop(){
        if(springContext != null){
            springContext.close(); // Ensure Spring context is closed when JavaFX stops
        }

        if (actorSystem != null) {
            System.out.println("Shutting down ActorSystem...");
            actorSystem.terminate();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
