package com.sunway.course.timetable.controller.app;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.sunway.course.timetable.controller.authentication.LoginSceneController;
import com.sunway.course.timetable.controller.base.ContentController;
import com.sunway.course.timetable.service.NavigationService;
import com.sunway.course.timetable.service.ProgrammeHistoryStorageService;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;


@Component
public class MainPageController extends ContentController {

    @FXML private ScrollPane recentTimetableScroll;
    @FXML private VBox recentTimetableBox;

    private final HostServices hostServices;
    private final ProgrammeHistoryStorageService programmeHistoryStorageService;

    public MainPageController(NavigationService navService,
                               LoginSceneController loginController,
                               HostServices hostServices,
                               ProgrammeHistoryStorageService programmeHistoryStorageService) {
        super(navService, loginController);
        this.hostServices = hostServices;
        this.programmeHistoryStorageService = programmeHistoryStorageService;
    }

    @Override
    protected void initialize() {
        super.initialize();
        subheading.setText("Dashboard");

        recentTimetableScroll.viewportBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            recentTimetableBox.setPrefWidth(newBounds.getWidth());
        });


        loadRecentTimetables(); 

    }

    private void loadRecentTimetables() {
        recentTimetableBox.getChildren().clear();
        File[] files = programmeHistoryStorageService.getProgrammeFiles();
        if (files == null || files.length == 0) {
            Label noFilesLabel = new Label("No timetables generated yet.");
            recentTimetableBox.getChildren().add(noFilesLabel);
            return;
        }

        List<File> sortedFiles = Arrays.stream(files)
                .sorted((f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()))
                .limit(10)
                .toList();

        for (File file : sortedFiles) {
            String displayName = file.getName().replace(".xslx", "");
            Button btn = new Button(displayName);
            btn.setMaxWidth(Double.MAX_VALUE);
            VBox.setMargin(btn, new Insets(5));
            btn.getStyleClass().add("timetable-button");

            btn.setOnAction(e -> {
                try {
                    hostServices.showDocument(file.toURI().toString());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            recentTimetableBox.getChildren().add(btn);
        }
    }

    public void refresh() {
        loadRecentTimetables();
    }
}



