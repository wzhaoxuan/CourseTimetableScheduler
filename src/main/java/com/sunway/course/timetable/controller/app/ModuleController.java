package com.sunway.course.timetable.controller.app;

import java.io.File;
import java.util.List;

import org.springframework.stereotype.Component;

import com.sunway.course.timetable.controller.authentication.LoginSceneController;
import com.sunway.course.timetable.controller.base.AbstractTimetableViewController;
import com.sunway.course.timetable.exporter.HistoricalTimetableExporter;
import com.sunway.course.timetable.result.SelectionStateHolder;
import com.sunway.course.timetable.service.ModuleServiceImpl;
import com.sunway.course.timetable.service.NavigationService;

import javafx.application.HostServices;

@Component
public class ModuleController extends AbstractTimetableViewController<String> {

    private final HistoricalTimetableExporter exporter;
    private final ModuleServiceImpl moduleService;

    public ModuleController(
        NavigationService navService,
        LoginSceneController loginController,
        SelectionStateHolder stateHolder,
        HistoricalTimetableExporter exporter,
        ModuleServiceImpl moduleService,
        HostServices hostServices) {
        super(navService, loginController, stateHolder, hostServices, id -> id);
        this.exporter = exporter;
        this.moduleService = moduleService;
    }

    @Override
    protected void initialize() {
        super.initialize();
        initializeBase();
        subheading.setText("View Module");

        List<String> moduleIds = exporter.getAllModuleIds();
        loadItems(moduleIds);
    }

    @Override
    protected void handleButtonClick(String moduleId) {
        try {
            List<File> files = exporter.exportByModule(moduleId);
            for (File file : files) {
                hostServices.showDocument(file.toURI().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

