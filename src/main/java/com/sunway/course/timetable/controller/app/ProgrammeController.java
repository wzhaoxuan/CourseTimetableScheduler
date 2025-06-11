package com.sunway.course.timetable.controller.app;
import java.io.File;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.sunway.course.timetable.controller.authentication.LoginSceneController;
import com.sunway.course.timetable.controller.base.AbstractTimetableViewController;
import com.sunway.course.timetable.exporter.HistoricalTimetableExporter;
import com.sunway.course.timetable.model.assignment.ModuleAssignmentData;
import com.sunway.course.timetable.service.NavigationService;
import com.sunway.course.timetable.service.PlanServiceImpl;

import javafx.application.HostServices;

@Component
public class ProgrammeController extends AbstractTimetableViewController<String> {

    private final HistoricalTimetableExporter exporter;
    private final Map<String, ModuleAssignmentData> moduleDataMap;
    private final PlanServiceImpl planService;  

    public ProgrammeController(
        NavigationService navService,
        LoginSceneController loginController,
        HistoricalTimetableExporter exporter,
        Map<String, ModuleAssignmentData> moduleDataMap,
        PlanServiceImpl planService,   
        HostServices hostServices) {
        super(navService, loginController, hostServices, id -> id);
        this.exporter = exporter;
        this.moduleDataMap = moduleDataMap;
        this.planService = planService;  
    }

    @Override
    protected void initialize() {
        super.initialize();
        initializeBase();
        subheading.setText("View Programme");

        List<String> programmeCodes = planService.getAllPlans().stream()
            .map(plan -> plan.getPlanContent().getModule())
            .map(module -> moduleDataMap.get(module.getId()))
            .filter(moduleData -> moduleData != null)
            .flatMap(moduleData -> moduleData.getProgrammeOfferingModules().stream())
            .map(programme -> programme.getProgrammeId().getId())
            .distinct()
            .sorted()
            .toList();


        loadItems(programmeCodes);
    }

    @Override
    protected void handleButtonClick(String programmeCode) {
        try {
            // You may add intake/year logic here as needed
            List<File> files = exporter.exportByProgramme(programmeCode, "January", 2025);
            for (File file : files) {
                hostServices.showDocument(file.toURI().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



