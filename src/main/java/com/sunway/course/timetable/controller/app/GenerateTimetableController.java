package com.sunway.course.timetable.controller.app;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.sunway.course.timetable.akka.actor.VenueCoordinatorActor;
import com.sunway.course.timetable.controller.authentication.LoginSceneController;
import com.sunway.course.timetable.controller.base.ContentController;
import com.sunway.course.timetable.evaluator.SatisfactionEvaluator;
import com.sunway.course.timetable.exporter.TimetableExcelExporter;
import com.sunway.course.timetable.result.FinalAssignmentResult;
import com.sunway.course.timetable.result.PreprocessingResult;
import com.sunway.course.timetable.service.LecturerServiceImpl;
import com.sunway.course.timetable.service.ModuleServiceImpl;
import com.sunway.course.timetable.service.NavigationService;
import com.sunway.course.timetable.service.PlanContentServiceImpl;
import com.sunway.course.timetable.service.PlanServiceImpl;
import com.sunway.course.timetable.service.SatisfactionServiceImpl;
import com.sunway.course.timetable.service.SessionServiceImpl;
import com.sunway.course.timetable.service.cluster.ProgrammeDistributionClustering;
import com.sunway.course.timetable.service.excelReader.LecturerAvailablityExcelReaderService;
import com.sunway.course.timetable.service.processor.ModuleAssignmentProcessor;
import com.sunway.course.timetable.service.processor.preprocessing.PreprocessingService;
import com.sunway.course.timetable.service.processor.preprocessing.SessionGroupPreprocessorService;
import com.sunway.course.timetable.service.venue.VenueAssignmentServiceImpl;
import com.sunway.course.timetable.service.venue.VenueDistanceServiceImpl;
import com.sunway.course.timetable.service.venue.VenueServiceImpl;
import com.sunway.course.timetable.service.venue.VenueSorterService;
import com.sunway.course.timetable.singleton.LecturerAvailabilityMatrix;
import com.sunway.course.timetable.singleton.StudentAvailabilityMatrix;
import com.sunway.course.timetable.singleton.VenueAvailabilityMatrix;
import com.sunway.course.timetable.util.InputUtil;
import com.sunway.course.timetable.util.LecturerDayAvailabilityUtil;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;


@Component
public class GenerateTimetableController extends ContentController {

    @FXML private Label programme, year, intake, validation, fileValidation;
    @FXML private ComboBox<String>  programmeChoice, yearChoice, intakeChoice;
    @FXML private Button generateButton, resetFilesButton;
    @FXML private Label dropTarget, instruction;
    @FXML private AnchorPane dropPane;
    @FXML private ListView<String> fileListView;
    @FXML private Region spacer1, spacer2, spacer4;

    private final List<File> droppedFiles = new ArrayList<>();
    private final TimetableController timetableController;
    private final PreprocessingService preprocessingService;
    private final LecturerAvailablityExcelReaderService lecturerAvailablityExcelReaderService;
    private final LecturerServiceImpl lecturerService;
    private final ModuleServiceImpl moduleService;
    private final VenueServiceImpl venueService;
    private final SessionServiceImpl sessionService;
    private final PlanContentServiceImpl planContentService;
    private final VenueDistanceServiceImpl venueDistanceService;
    private final VenueAssignmentServiceImpl venueAssignmentService;
    private final VenueSorterService venueSorterService;
    private final PlanServiceImpl planService;
    private final SatisfactionServiceImpl satisfactionService;
    private final SessionGroupPreprocessorService sessionGroupPreprocessorService;
    private final VenueAvailabilityMatrix venueMatrix;
    private final LecturerAvailabilityMatrix lecturerMatrix;
    private final StudentAvailabilityMatrix studentMatrix;
    private final ActorSystem<Void> actorSystem;
    private final ActorRef<VenueCoordinatorActor.VenueCoordinatorCommand> venueCoordinatorActor;
    private final ProgrammeDistributionClustering clustering;
    private final TimetableExcelExporter timetableExcelExporter;
    private final LecturerDayAvailabilityUtil lecturerDayAvailabilityUtil;
    private final SatisfactionEvaluator satisfactionEvaluator;

    // == matches file names in the ListView ==
    private File subjectPlanFile;
    private File moduleSemFile;
    private File studentSemFile;
    private File lecturerAvailabilityFile;
    private List<File> fifthFile = new ArrayList<>(); 
    

    public GenerateTimetableController(NavigationService navService,
                                    LoginSceneController loginController,
                                    TimetableController timetableController,
                                    PreprocessingService preprocessingService,
                                    LecturerAvailablityExcelReaderService lecturerAvailablityExcelReaderService,
                                    LecturerServiceImpl lecturerService,
                                    ModuleServiceImpl moduleService,
                                    VenueServiceImpl venueService,
                                    SessionServiceImpl sessionService,
                                    PlanContentServiceImpl planContentService,
                                    VenueDistanceServiceImpl venueDistanceService,
                                    VenueAssignmentServiceImpl venueAssignmentService,
                                    VenueSorterService venueSorterService,
                                    PlanServiceImpl planService,
                                    SatisfactionServiceImpl satisfactionService,
                                    SessionGroupPreprocessorService sessionGroupPreprocessorService,
                                    VenueAvailabilityMatrix venueMatrix,
                                    LecturerAvailabilityMatrix lecturerMatrix,
                                    StudentAvailabilityMatrix studentMatrix,
                                    ActorSystem<Void> actorSystem,
                                    ActorRef<VenueCoordinatorActor.VenueCoordinatorCommand> venueCoordinatorActor,
                                    ProgrammeDistributionClustering clustering,
                                    TimetableExcelExporter timetableExcelExporter,
                                    LecturerDayAvailabilityUtil lecturerDayAvailabilityUtil,
                                    SatisfactionEvaluator satisfactionEvaluator,
                                    JdbcTemplate jdbcTemplate) {
        super(navService, loginController);
        this.timetableController = timetableController;
        this.preprocessingService = preprocessingService;
        this.lecturerAvailablityExcelReaderService = lecturerAvailablityExcelReaderService;
        this.lecturerService = lecturerService;
        this.moduleService = moduleService;
        this.venueService = venueService;
        this.sessionService = sessionService;
        this.planContentService = planContentService;
        this.venueDistanceService = venueDistanceService;
        this.venueAssignmentService = venueAssignmentService;
        this.venueSorterService = venueSorterService;
        this.planService = planService;
        this.satisfactionService = satisfactionService;
        this.sessionGroupPreprocessorService = sessionGroupPreprocessorService;
        this.venueMatrix = venueMatrix;
        this.lecturerMatrix = lecturerMatrix;
        this.studentMatrix = studentMatrix;
        this.actorSystem = actorSystem;
        this.venueCoordinatorActor = venueCoordinatorActor;
        this.clustering = clustering;
        this.timetableExcelExporter = timetableExcelExporter;
        this.lecturerDayAvailabilityUtil = lecturerDayAvailabilityUtil;
        this.satisfactionEvaluator = satisfactionEvaluator;
    }


    @Override
    public void initialize() {
        super.initialize();
        setupLabelsText();
        setupComboBoxes();
        setupLayout();
        setupDragAndDrop(); 

        fileValidation.setVisible(false);
        validation.setVisible(false);
        resetFilesButton.setOnAction(event -> clearDroppedFiles());
        clearDroppedFiles();
    }

    @FXML
    private void generate() {
        // 1) pull values and validate as before
        String programme = programmeChoice.getValue();
        String year = yearChoice.getValue();
        String intake = intakeChoice.getValue();

        boolean hasError = false;

        if (programme == null || year == null || intake == null) {
            validation.setText("*Please select Programme, Year,Intake.");
            validation.setVisible(true);
            hasError = true;
        } else {
            validation.setVisible(false); // hide error if resolved
        }

        // Step 1: Read Excel (use fixed path or let user upload in future)
        if (subjectPlanFile == null || moduleSemFile == null || studentSemFile == null || lecturerAvailabilityFile == null) {
            fileValidation.setText("*Please drop all 4 required .xlsx files.");
            fileValidation.setVisible(true);
            hasError = true;
        } else {
            fileValidation.setVisible(false); // hide error if resolved
        }

        if(hasError) return;

        // Use these files in your processor
        System.out.println("Subject Plan: " + subjectPlanFile.getAbsolutePath());
        System.out.println("Module Sem: " + moduleSemFile.getAbsolutePath());
        System.out.println("Student Sem: " + studentSemFile.getAbsolutePath());

        generateButton.setText("Generating...");
        generateButton.setDisable(true);
        resetFilesButton.setDisable(true);

        final AtomicReference<FinalAssignmentResult> resultRef = new AtomicReference<>();
        final Set<String> errors = new LinkedHashSet<>();

        new Thread(() -> {
            try {
                PreprocessingResult pre = preprocessingService
                    .preprocessModuleAndStudents(
                        subjectPlanFile.getAbsolutePath(),
                        moduleSemFile.getAbsolutePath(),
                        studentSemFile.getAbsolutePath()
                    );

                try {
                    lecturerAvailablityExcelReaderService
                        .readLecturerAvailabilityExcelFile(
                            lecturerAvailabilityFile.getAbsolutePath()
                        );
                } catch (IllegalStateException ex) {
                    errors.addAll(List.of(ex.getMessage().split("\n")));
                } catch (IOException ioe) {
                    errors.add("Failed to read lecturer availability: " + ioe.getMessage());
                }

                if (errors.isEmpty()) {
                    ModuleAssignmentProcessor proc = new ModuleAssignmentProcessor(
                        lecturerService, moduleService, venueService,
                        sessionService, planContentService, venueDistanceService,
                        venueAssignmentService, venueSorterService, planService,
                        satisfactionService, sessionGroupPreprocessorService,
                        venueMatrix, lecturerMatrix, studentMatrix,
                        actorSystem, venueCoordinatorActor, clustering,
                        timetableExcelExporter, lecturerDayAvailabilityUtil,
                        satisfactionEvaluator
                    );

                    resultRef.set(proc.processAssignments(
                        pre.getModuleAssignmentDataList(),
                        pre.getStudentProgrammeMap(),
                        pre.getStudentSemesterMap(),
                        programme, intake, Integer.parseInt(year)
                    ));
                }
            } catch (Exception ex) {
                errors.add("Unexpected error: " + ex.getMessage());
            }

            // 4) when done (success or error), hop back to the FX thread
            Platform.runLater(() -> {
                try{
                    if (!errors.isEmpty()) {
                    showErrorPage(number(errors));
                } else {
                    FinalAssignmentResult result = resultRef.get();
                    navigationService.loadTimetablePage();
                    timetableController.loadExportedTimetables(
                        result.getExportedTimetableFiles(),
                        result.getLecturerTimetableFiles(),
                        result.getModuleTimetableFiles()
                    );
                }
                } catch (Exception e) {
                    e.printStackTrace(); // Print the error if something goes wrong
                } finally {
                    generateButton.setText("Generate");
                    generateButton.setDisable(false);
                    resetFilesButton.setDisable(false);
                }
            });
        }, "Timetable-Generator-Thread").start();
    }

    private void setupDragAndDrop() {
        dropPane.setOnDragOver(event -> {
            if (event.getGestureSource() != dropPane && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        dropPane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasFiles()) {
                List<File> files = db.getFiles().stream()
                    .filter(file -> file.getName().toLowerCase().endsWith(".xlsx"))
                    .collect(Collectors.toList());

                for (File file : files) {
                    String name = file.getName().toLowerCase();
                    if (name.contains("subjectplan")) {
                        subjectPlanFile = file;
                    } else if (name.contains("modulesem")) {
                        moduleSemFile = file;
                    } else if (name.contains("studentsem")) {
                        studentSemFile = file;
                    } else if (name.contains("lectureravailability")) {
                        lecturerAvailabilityFile = file;
                    } else {
                        fifthFile.add(file); // fallback file
                    }

                    // Prevent duplicates
                    if (droppedFiles.stream().noneMatch(f -> f.getName().equalsIgnoreCase(file.getName()))) {
                        droppedFiles.add(file);
                    }
                }

                updateFileListView();
                success = true;
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void updateFileListView() {
        fileListView.getItems().clear();

        if (subjectPlanFile != null) {
            fileListView.getItems().add("Subject Plan: " + subjectPlanFile.getName());
        }
        if (moduleSemFile != null) {
            fileListView.getItems().add("Module-Semester: " + moduleSemFile.getName());
        }
        if (studentSemFile != null) {
            fileListView.getItems().add("Student-Semester: " + studentSemFile.getName());
        }
        if (lecturerAvailabilityFile != null) {
            fileListView.getItems().add("Lecturer Availability: " + lecturerAvailabilityFile.getName());
        }
        if (fifthFile != null) {
            fileListView.getItems().add("Extra: " + fifthFile.stream()
                                                            .map(f -> f.getName())
                                                            .collect(Collectors.joining(", ")));
        }
    }

    private void clearDroppedFiles() {
        droppedFiles.clear();
        subjectPlanFile = null;
        moduleSemFile = null;
        studentSemFile = null;
        lecturerAvailabilityFile = null;
        fifthFile = null;
        fileListView.getItems().clear();
        showSuccess("Cleared file selection.");
    }

    private void setupLabelsText() {
        subheading.setText("Generate Timetable");
        programme.setText("Programme:");
        year.setText("Year:");
        intake.setText("Intake:");
        generateButton.setText("Generate");
        resetFilesButton.setText("Reset");
        dropTarget.setText("Drop files here");
        instruction.setText("Please drop .xlsx files:\n SubjectPlan, ModuleSem, StudentSem, LecturerAvailability.");
        instruction.setWrapText(true);
    }

    private void setupComboBoxes() {
        programmeChoice.getItems().addAll("BIT", "BSE", "BCS");
        programmeChoice.setVisibleRowCount(5);
        yearChoice.getItems().addAll(InputUtil.getYearOptions());
        yearChoice.setVisibleRowCount(5);
        intakeChoice.getItems().addAll(InputUtil.getIntake());
        intakeChoice.setVisibleRowCount(5);
    }

    private void setupLayout() {
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        HBox.setHgrow(spacer4, Priority.ALWAYS);
    }

    private void showSuccess(String message) {
        System.out.println(message); // Same here
    }

    private void showErrorPage(String message) {
        try {
            navigationService.loadErrorPage(message);
        }
        catch (Exception ex) {
            // fallback: log, or pop up a simple alert
            ex.printStackTrace();
        }
    }

    private String number(Set<String> msgs) {
        var sb = new StringBuilder();
        int i = 1;
        for (String m : msgs) {
            sb.append(i++).append(". ").append(m).append("\n");
        }
        // remove last newline
        if (sb.length() > 0) sb.setLength(sb.length()-1);
        return sb.toString();
    }
}
