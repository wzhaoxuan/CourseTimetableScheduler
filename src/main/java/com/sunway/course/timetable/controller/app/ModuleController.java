package com.sunway.course.timetable.controller.app;

import java.io.File;
import java.util.List;
import java.util.Comparator;

import org.springframework.stereotype.Component;

import com.sunway.course.timetable.controller.authentication.LoginSceneController;
import com.sunway.course.timetable.controller.base.AbstractTimetableViewController;
import com.sunway.course.timetable.exporter.HistoricalTimetableExporter;
import com.sunway.course.timetable.result.SelectionStateHolder;
import com.sunway.course.timetable.service.ModuleServiceImpl;
import com.sunway.course.timetable.service.NavigationService;
import com.sunway.course.timetable.service.PlanServiceImpl;
import com.sunway.course.timetable.result.ModuleVersionItem;

import javafx.application.HostServices;

@Component
public class ModuleController extends AbstractTimetableViewController<ModuleVersionItem> {

    private final HistoricalTimetableExporter exporter;
    private final PlanServiceImpl planService;

    public ModuleController(
        NavigationService navService,
        LoginSceneController loginController,
        SelectionStateHolder stateHolder,
        HistoricalTimetableExporter exporter,
        PlanServiceImpl planService,
        HostServices hostServices) {
        super(navService, loginController, stateHolder, hostServices, ModuleVersionItem::getDisplayName);
        this.exporter = exporter;
        this.planService = planService;
    }

    @Override
    protected void initialize() {
        super.initialize();
        initializeBase();
        subheading.setText("View Module");

        List<String> moduleIds = exporter.getAllModuleIds();
        List<ModuleVersionItem> items = moduleIds.stream()
            .flatMap(id -> planService.getAllVersionsByModule(id).stream()
                .map(version -> new ModuleVersionItem(id, version)))
            .sorted(Comparator.comparing(ModuleVersionItem::getDisplayName))
            .toList();

        loadItems(items);
    }

    @Override
    protected void handleButtonClick(ModuleVersionItem item) {
        try {
            List<File> files = exporter.exportByModule(item.getModuleId(), item.getVersion());
            for (File file : files) {
                hostServices.showDocument(file.toURI().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

