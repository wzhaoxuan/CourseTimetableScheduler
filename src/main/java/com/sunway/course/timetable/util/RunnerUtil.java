package com.sunway.course.timetable.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sunway.course.timetable.service.generator.VenueDistanceGenerator;



@Configuration
public class RunnerUtil {
    Logger logger = LoggerFactory.getLogger(RunnerUtil.class);


    @Bean
    public CommandLineRunner generateVenueDistances(VenueDistanceGenerator venueDistanceGenerator) {
        return args -> {
            logger.info(">>> TESTING LOGGER OUTPUT <<<");
            // Generate venue distances
            venueDistanceGenerator.generateVenueDistances();
            System.out.println("Venue distances saved into database.");
        };
    }

    // @Bean
    // public CommandLineRunner readExcelFile(ModuleExcelReaderService excelReaderService) {
    //     return args -> {
    //         // Read Excel file and process data
    //         String filePath = "src/main/resources/file/SubjectPlan.xlsx";
    //         try {
    //             List<SubjectPlanInfo> subjectPlanInfos = excelReaderService.readExcelFile(filePath);
    //             for(int i = 0; i < 3; i++){
    //                 SubjectPlanInfo subjectPlanInfo = subjectPlanInfos.get(i);
    //                 System.out.println("Subject Plan Info " + (i + 1) + ": " + subjectPlanInfo.getSubjectCode() + ", " + subjectPlanInfo.getSubjectName());
    //             }

    //         } catch (Exception e) {
    //             System.err.println("Error reading Excel file: " + e.getMessage());
    //             e.printStackTrace();
    //         }
    //     };
    // }

    // @Bean
    // public CommandLineRunner programmeExcelReader(ProgrammeExcelReaderService programmeExcelReaderService) {
    //     return args -> {
    //         // Read Excel file and process data
    //         String filePath = "src/main/resources/file/ProgrammeDetails.xlsx";
    //         try {
    //             logger.info(">>> RUNNING PROGRAMME OUTPUT <<<");
    //             programmeExcelReaderService.processProgrammeExcelData(filePath);
    //         } catch (Exception e) {
    //             System.err.println("Error reading Excel file: " + e.getMessage());
    //             e.printStackTrace();
    //         }
    //     };
    // }

    // @Bean
    // public CommandLineRunner readlecturerUnavailableExcel(LecturerAvailablityExcelReaderService lecturerAvailablityExcelReaderService) {
    //     return args -> {
    //         String filePath = "src/main/resources/file/LecturerUnavailable.xlsx";
    //         try {
    //             // Read the Lecturer Availability Excel file

    //             lecturerAvailablityExcelReaderService.readLecturerAvailabilityExcelFile(filePath);
    //             System.out.println("Lecturer availability processed successfully.");
    //         } catch (Exception e) {
    //             System.err.println("Error reading Lecturer Availability Excel file: " + e.getMessage());
    //             e.printStackTrace();
    //         }
    //     };
    // }

    // @Bean
    // @Profile("!test")  // Exclude from tests
    // public CommandLineRunner ModeleDataProcessor(PreprocessingService preprocessingService,
    //                                                 LecturerServiceImpl lecturerService,
    //                                                 ModuleServiceImpl moduleService,
    //                                                 SessionServiceImpl sessionService,
    //                                                 PlanContentServiceImpl planContentService,
    //                                                 VenueDistanceServiceImpl venueDistanceService,
    //                                                 VenueAssignmentServiceImpl venueAssignmentService,
    //                                                 VenueSorterService venueSorterService,
    //                                                 SessionGroupPreprocessorService sessionGroupPreprocessorService,
    //                                                 VenueAvailabilityMatrix venueMatrix,
    //                                                 LecturerAvailabilityMatrix lecturerMatrix,
    //                                                 StudentAvailabilityMatrix studentMatrix,
    //                                                 ActorSystem<Void> actorSystem,
    //                                                 ActorRef<VenueCoordinatorActor.VenueCoordinatorCommand> venueCoordinatorActor,
    //                                                 ProgrammeDistributionClustering clustering,
    //                                                 TimetableExcelExporter timetableExcelExporter,
    //                                                 LecturerDayAvailabilityUtil lecturerDayAvailabilityUtil,
    //                                                 FitnessEvaluator fitnessEvaluator) {
    //     return args -> {
    //         try {
    //             String subjectPlanFilePath = "src/main/resources/file/SubjectPlan.xlsx";
    //             String moduleSemFilePath = "src/main/resources/file/ModuleSem.xlsx";
    //             String studentSemFilePath = "src/main/resources/file/StudentSem.xlsx";

    //             PreprocessingResult preprocessingResult = preprocessingService
    //                     .preprocessModuleAndStudents(subjectPlanFilePath, moduleSemFilePath, studentSemFilePath);

    //             // System.out.println("=== Combined Module Assignment Summary ===");
    //             // for (ModuleAssignmentData data : preprocessingResult.getModuleAssignmentDataList()) {
    //             //     String moduleId = data.getModule().getId();
    //             //     String subjectTitle = data.getSubjectPlanInfo().getSubjectCode();
    //             //     Set<Long> studentIds = data.getEligibleStudents().stream()
    //             //         .map(Student::getId)
    //             //         .collect(Collectors.toSet());

    //             //     System.out.println("Module ID: " + moduleId + ", Title: " + subjectTitle);
    //             //     System.out.println("  Total Students: " + studentIds.size());
    //             //     System.out.println("  Student IDs: " + studentIds.stream()
    //             //         .sorted()
    //             //         .map(String::valueOf)
    //             //         .collect(Collectors.joining(", ")));
    //             // }
    //             // System.out.println("===================================================");

    //             // Manually create the processor
    //             ModuleAssignmentProcessor processor = new ModuleAssignmentProcessor(lecturerService,
    //                                                                                 moduleService,
    //                                                                                 sessionService,
    //                                                                                 planContentService,
    //                                                                                 venueDistanceService,
    //                                                                                 venueAssignmentService,
    //                                                                                 venueSorterService,
    //                                                                                 sessionGroupPreprocessorService,
    //                                                                                 venueMatrix,
    //                                                                                 lecturerMatrix,
    //                                                                                 studentMatrix,
    //                                                                                 actorSystem,
    //                                                                                 venueCoordinatorActor,
    //                                                                                 clustering,
    //                                                                                 timetableExcelExporter,
    //                                                                                 lecturerDayAvailabilityUtil,
    //                                                                                 fitnessEvaluator);

    //             // Run the assignment
    //             Map<Integer, Map<String, List<Session>>> session = processor.processAssignments(preprocessingResult.getModuleAssignmentDataList(), preprocessingResult.getStudentProgrammeMap(), preprocessingResult.getStudentSemesterMap());

    //         } catch (Exception e) {
    //             System.err.println("Error reading Excel file: " + e.getMessage());
    //             e.printStackTrace();
    //         }
    //     };
    // }


    // @Bean
    // public CommandLineRunner StudentSemProcessor(StudentSemExcelReaderService studentSemesterService) {
    //     return args -> {
    //         String filePath = "src/main/resources/file/StudentSem.xlsx";

    //         try {
    //             // Read the Student Semester Excel file
    //             studentSemesterService.readStudentSemExcelFile(filePath);
    //         } catch (FileNotFoundException e) {
    //             logger.error("Student Semester Excel file not found: {}", e.getMessage());
    //         }
    //         logger.info(">>> Student Semester Processing Runner Initialized <<<");
    //         // Example: studentSemesterService.processStudentSemesters();
    //     };
    // }

    // @Bean
    // public CommandLineRunner ModuleSemProcessor(ModuleSemExcelReaderService moduleSemExcelReaderService) {
    //     return args -> {
    //         String filePath = "src/main/resources/file/ModuleSem.xlsx";

    //         try {
    //             // Read the Module Semester Excel file
    //             moduleSemExcelReaderService.readModuleSemExcelFile(filePath);
    //         } catch (FileNotFoundException e) {
    //             logger.error("Module Semester Excel file not found: {}", e.getMessage());
    //         }
    //         logger.info(">>> Module Semester Processing Runner Initialized <<<");
    //     };
    // }



}
