package com.sunway.course.timetable.controller.app;
import java.io.File;
import java.util.List;

import org.springframework.stereotype.Component;

import com.sunway.course.timetable.controller.authentication.LoginSceneController;
import com.sunway.course.timetable.controller.base.AbstractTimetableViewController;
import com.sunway.course.timetable.exporter.HistoricalTimetableExporter;
import com.sunway.course.timetable.result.ModuleDataHolder;
import com.sunway.course.timetable.result.SelectionStateHolder;
import com.sunway.course.timetable.service.NavigationService;

import javafx.application.HostServices;

@Component
public class ProgrammeController extends AbstractTimetableViewController<String> {

    private final HistoricalTimetableExporter exporter;
    private final ModuleDataHolder moduleDataHolder;

    public ProgrammeController(
        NavigationService navService,
        LoginSceneController loginController,
        SelectionStateHolder stateHolder,
        HistoricalTimetableExporter exporter,
        ModuleDataHolder moduleDataHolder,
        HostServices hostServices) {

        super(navService, loginController, stateHolder, hostServices, id -> id);
        this.exporter = exporter;
        this.moduleDataHolder = moduleDataHolder;
    }

    @Override
    protected void initialize() {
        super.initialize();
        initializeBase();
        subheading.setText("View Programme");

        // No more moduleDataMap / planService involved
        List<String> programmeCodes = moduleDataHolder.getModuleDataList().stream()
            .flatMap(data -> data.getProgrammeOfferingModules().stream())
            .map(p -> p.getProgrammeId().getId())
            .distinct()
            .sorted()
            .toList();

        loadItems(programmeCodes);
    }

    @Override
    protected void handleButtonClick(String programmeCode) {
        try {
            List<File> files = exporter.exportByProgramme(programmeCode, "January", 2025);
            for (File file : files) {
                hostServices.showDocument(file.toURI().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}




