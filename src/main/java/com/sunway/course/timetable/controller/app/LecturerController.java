package com.sunway.course.timetable.controller.app;
import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.sunway.course.timetable.controller.authentication.LoginSceneController;
import com.sunway.course.timetable.controller.base.AbstractTimetableViewController;
import com.sunway.course.timetable.exporter.HistoricalTimetableExporter;
import com.sunway.course.timetable.model.Lecturer;
import com.sunway.course.timetable.result.LecturerVersionItem;
import com.sunway.course.timetable.result.SelectionStateHolder;
import com.sunway.course.timetable.service.LecturerServiceImpl;
import com.sunway.course.timetable.service.NavigationService;
import com.sunway.course.timetable.service.PlanServiceImpl;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

@Component
public class LecturerController extends AbstractTimetableViewController<LecturerVersionItem> {

    @FXML private RadioButton full_time, part_time, teaching_assistant;

    private final LecturerServiceImpl lecturerService;
    private final HistoricalTimetableExporter exporter;
    private final PlanServiceImpl planService;

    public LecturerController(
        NavigationService navService,
        LoginSceneController loginController,
        SelectionStateHolder stateHolder,
        HistoricalTimetableExporter exporter,
        LecturerServiceImpl lecturerService,
        HostServices hostServices,
        PlanServiceImpl planService) {
        super(navService, loginController, stateHolder, hostServices, LecturerVersionItem::getDisplayName); 
        this.lecturerService = lecturerService;
        this.exporter = exporter;
        this.planService = planService;
    }

    @Override
    protected void initialize() {
        super.initialize();
        initializeBase();
        subheading.setText("View Lecturer");
        full_time.setText("Full time");
        part_time.setText("Part time");
        teaching_assistant.setText("Teaching assistant");

        ToggleGroup typeGroup = new ToggleGroup();
        full_time.setToggleGroup(typeGroup);
        part_time.setToggleGroup(typeGroup);
        teaching_assistant.setToggleGroup(typeGroup);

        full_time.setOnAction(e -> loadLecturersByType("FullTime"));
        part_time.setOnAction(e -> loadLecturersByType("PartTime"));
        teaching_assistant.setOnAction(e -> loadLecturersByType("TeachingAssistant"));

        loadLecturersWithPlans();
    }

    private void loadLecturersWithPlans() {
        Set<String> lecturers = planService.getAllPlans().stream()
            .map(plan -> plan.getPlanContent().getSession().getLecturer().getName())
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        List<LecturerVersionItem> items = lecturers.stream()
            .flatMap(name -> planService.getAllVersionsByLecturer(name).stream()
                .map(version -> new LecturerVersionItem(name, version)))
            .sorted(Comparator.comparing(LecturerVersionItem::getDisplayName))
            .toList();

        loadItems(items);
    }


    private void loadLecturersByType(String type) {
        Set<String> lecturersWithPlans = planService.getAllPlans().stream()
            .map(plan -> plan.getPlanContent().getSession().getLecturer().getName())
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        List<LecturerVersionItem> items = lecturerService.getLecturersByType(type)
            .orElse(List.of())
            .stream()
            .map(Lecturer::getName)
            .filter(lecturersWithPlans::contains)
            .flatMap(name -> planService.getAllVersionsByLecturer(name).stream()
                .map(version -> new LecturerVersionItem(name, version)))
            .sorted(Comparator.comparing(LecturerVersionItem::getDisplayName))
            .toList();

        loadItems(items);
    }


    @Override
    protected void handleButtonClick(LecturerVersionItem item) {
        try {
            List<File> files = exporter.exportByLecturer(item.getName(), item.getVersion());
            for (File file : files) {
                hostServices.showDocument(file.toURI().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


