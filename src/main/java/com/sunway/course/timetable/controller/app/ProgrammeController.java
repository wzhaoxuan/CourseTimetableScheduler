package com.sunway.course.timetable.controller.app;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.sunway.course.timetable.controller.authentication.LoginSceneController;
import com.sunway.course.timetable.controller.base.AbstractTimetableViewController;
import com.sunway.course.timetable.result.SelectionStateHolder;
import com.sunway.course.timetable.service.NavigationService;
import com.sunway.course.timetable.service.ProgrammeHistoryStorageService;

import javafx.application.HostServices;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

@Component
public class ProgrammeController extends AbstractTimetableViewController<String> {

    private final ProgrammeHistoryStorageService historyStorageService;

    public ProgrammeController(
        NavigationService navService,
        LoginSceneController loginController,
        SelectionStateHolder stateHolder,
        ProgrammeHistoryStorageService historyStorageService,
        HostServices hostServices) {

        super(navService, loginController, stateHolder, hostServices, id -> id);
        this.historyStorageService = historyStorageService;
    }

    @Override
    protected void initialize() {
        super.initialize();
        initializeBase();
        subheading.setText("View Programme");

        List<String> filenames = getAllProgrammeFiles();
        loadItems(filenames);
    }

    private List<String> getAllProgrammeFiles() {
        File[] files = historyStorageService.getProgrammeFiles();
        if (files == null) return List.of();

        return Arrays.stream(files)
            .map(file -> file.getName())
            .distinct()
            .sorted()
            .toList();
    }

    @Override
    protected void handleButtonClick(String fullFilename) {
        try {
            File file = new File("system_storage/history/programme/" + fullFilename);
            hostServices.showDocument(file.toURI().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void displayButtons(List<String> items) {
        buttonBox.getChildren().clear();
        for (String fullFilename : items) {
            String displayName = fullFilename.replace(".xlsx", "");
            Button btn = new Button(displayName);
            btn.setMaxWidth(Double.MAX_VALUE);
            VBox.setMargin(btn, new Insets(5));
            btn.getStyleClass().add("timetable-button");

            btn.setOnAction(e -> handleButtonClick(fullFilename));
            buttonBox.getChildren().add(btn);
        }
    }
}





