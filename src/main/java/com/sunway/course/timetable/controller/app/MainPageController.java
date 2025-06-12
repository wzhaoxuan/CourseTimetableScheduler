package com.sunway.course.timetable.controller.app;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.sunway.course.timetable.controller.authentication.LoginSceneController;
import com.sunway.course.timetable.controller.base.ContentController;
import com.sunway.course.timetable.result.MainPageStateHolder;
import com.sunway.course.timetable.service.NavigationService;

import javafx.application.HostServices;
import javafx.application.Platform;
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
    private final MainPageStateHolder mainPageStateHolder;

    private final List<File> recentSemesterFiles = new ArrayList<>();

    public MainPageController(NavigationService navService,
                               LoginSceneController loginController,
                               HostServices hostServices,
                               MainPageStateHolder mainPageStateHolder) {
        super(navService, loginController);
        this.hostServices = hostServices;
        this.mainPageStateHolder = mainPageStateHolder;
    }

    @Override
    protected void initialize() {
        super.initialize();
        subheading.setText("Dashboard");

        recentTimetableScroll.viewportBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            recentTimetableBox.setPrefWidth(newBounds.getWidth());
        });

        // Nothing to load initially — only when timetable generated
        Platform.runLater(this::refresh);
    }

    public void refresh() {
        recentSemesterFiles.clear();

        List<File> files = mainPageStateHolder.getLatestSemesterFiles();
        if (files != null && !files.isEmpty()) {
            recentSemesterFiles.addAll(files);
        }

        refreshDisplayedTimetables();
    }

    private void refreshDisplayedTimetables() {
        recentTimetableBox.getChildren().clear();

        if (recentSemesterFiles.isEmpty()) return;

        Label title = new Label("Recent Timetables");
        title.getStyleClass().add("section-label");
        recentTimetableBox.getChildren().add(title);

        for (File file : recentSemesterFiles) {
            String displayName = file.getName().replace(".xlsx", "");
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
}



