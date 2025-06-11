package com.sunway.course.timetable.controller.app;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

@Component
public class TimetableController extends ContentController {

    @FXML Button downloadAll;
    @FXML private ScrollPane timetableScrollPane;
    @FXML private VBox timetableList;

    private final List<File> exportedTimetables = new ArrayList<>();
    private final List<File> semesterFiles = new ArrayList<>();
    private final List<File> lecturerFiles = new ArrayList<>();
    private final List<File> moduleFiles = new ArrayList<>();

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
        timetableScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        // Bind VBox width to the viewport width of the ScrollPane
        timetableScrollPane.viewportBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            timetableList.setPrefWidth(newBounds.getWidth());
        });

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
        fileChooser.setTitle("Save All Timetables");
        fileChooser.setInitialFileName("timetables.zip");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP Files", "*.zip"));
        File destination = fileChooser.showSaveDialog(downloadAll.getScene().getWindow());

        if (destination != null) {
            try {
                Map<String, List<File>> fileGroups = new HashMap<>();
                fileGroups.put("semester", semesterFiles); 
                fileGroups.put("lecturer", lecturerFiles); 
                fileGroups.put("module", moduleFiles); 

                FileUtils.zipFilesWithStructure(fileGroups, destination);
                System.out.println("All timetables zipped into: " + destination.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void loadExportedTimetables(List<File> timetableFiles, List<File> lecturerFiles, List<File> moduleFiles, double fitnessScore) {
        exportedTimetables.clear();
        exportedTimetables.addAll(timetableFiles);
        exportedTimetables.addAll(lecturerFiles);
        exportedTimetables.addAll(moduleFiles);

        this.semesterFiles.clear();
        this.lecturerFiles.clear();
        this.moduleFiles.clear();
        this.semesterFiles.addAll(timetableFiles);
        this.lecturerFiles.addAll(lecturerFiles);
        this.moduleFiles.addAll(moduleFiles);

        timetableList.getChildren().clear();

        if (!timetableFiles.isEmpty()) {
            Label semLabel = new Label("Semester Timetables");
            semLabel.getStyleClass().add("section-label");
            timetableList.getChildren().add(semLabel);
        }

        for (File file : timetableFiles) {
            addDownloadButton(file);
        }

        if (!lecturerFiles.isEmpty()) {
            Label lecturerLabel = new Label("Lecturer Timetables");
            lecturerLabel.getStyleClass().add("section-label");
            VBox.setMargin(lecturerLabel, new Insets(10, 0, 0, 0));
            timetableList.getChildren().add(lecturerLabel);
        }

        for (File file : lecturerFiles) {
            addDownloadButton(file);
        }

        if (!moduleFiles.isEmpty()) {
            Label moduleLabel = new Label("Module Timetables");
            moduleLabel.getStyleClass().add("section-label");
            VBox.setMargin(moduleLabel, new Insets(10, 0, 0, 0));
            timetableList.getChildren().add(moduleLabel);
        }

        for (File file : moduleFiles) {
            addDownloadButton(file);
        }
    }

    private void addDownloadButton(File file) {
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
