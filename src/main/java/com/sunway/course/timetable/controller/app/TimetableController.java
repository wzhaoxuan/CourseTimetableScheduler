package com.sunway.course.timetable.controller.app;
import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.sunway.course.timetable.controller.authentication.LoginSceneController;
import com.sunway.course.timetable.controller.base.ContentController;
import com.sunway.course.timetable.service.NavigationService;
import com.sunway.course.timetable.util.FileUtils;
import com.sunway.course.timetable.view.MainApp;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

@Component
public class TimetableController extends ContentController {

    @FXML Button downloadAll;
    @FXML private VBox timetableList;

    private final List<File> exportedTimetables = new ArrayList<>();
    private final HostServices hostServices;

    public TimetableController(NavigationService navService, LoginSceneController loginController,
                              HostServices hostServices) {
        super(navService, loginController);
        this.hostServices = MainApp.hostServices;
    }

    @Override
    protected void initialize() {
        super.initialize(); 
        setupLabelsText();
    }

    private void setupLabelsText() {
        subheading.setText("Timetable");
        downloadAll.setText("Download");
    }

    @FXML
    public void downloadAll() {
        if (exportedTimetables.isEmpty()) {
            System.out.println("No timetables available to download.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Folder to Save Files");
        fileChooser.setInitialFileName("timetables.zip");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP Files", "*.zip"));
        File destination = fileChooser.showSaveDialog(downloadAll.getScene().getWindow());

        if (destination != null) {
            try {
                FileUtils.zipFiles(exportedTimetables, destination); // helper you’ll define next
                System.out.println("All timetables zipped to: " + destination.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void loadExportedTimetables(List<File> timetableFiles, double fitnessScore) {
        exportedTimetables.clear();
        exportedTimetables.addAll(timetableFiles);

        timetableList.getChildren().clear();

        for (File file : timetableFiles) {
            String displayName = file.getName().replace(".xlsx", "");
            Button btn = new Button(displayName);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.getStyleClass().add("timetable-button"); 
            VBox.setMargin(btn, new Insets(5));
            btn.setOnAction(e -> {
                try {
                    hostServices.showDocument(file.toURI().toString());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            timetableList.getChildren().add(btn);
        }
    }
}
