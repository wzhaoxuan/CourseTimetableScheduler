package com.sunway.course.timetable.util;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.sunway.course.timetable.engine.ConstraintEngine;
import com.sunway.course.timetable.model.assignment.ModuleAssignmentData;
import com.sunway.course.timetable.model.assignment.PreprocessingResult;
import com.sunway.course.timetable.service.LecturerServiceImpl;
import com.sunway.course.timetable.service.cluster.ProgrammeDistributionClustering;
import com.sunway.course.timetable.service.generator.VenueDistanceGenerator;
import com.sunway.course.timetable.service.processor.ModuleAssignmentProcessor;
import com.sunway.course.timetable.service.processor.preprocessing.PreprocessingService;
import com.sunway.course.timetable.VenueAssignApp;

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
    //             programmeExcelReaderService.processProgrammeExcelData(filePath);
    //         } catch (Exception e) {
    //             System.err.println("Error reading Excel file: " + e.getMessage());
    //             e.printStackTrace();
    //         }
    //     };
    // }

    // @Bean
    // @Profile("!test")  // Exclude from tests
    // public CommandLineRunner ModeleDataProcessor(PreprocessingService preprocessingService,
    //                                              LecturerServiceImpl lecturerService,
    //                                              ConstraintEngine constraintEngine,
    //                                              ProgrammeDistributionClustering clustering) {
    //     return args -> {
    //         try {
    //             String subjectPlanfilePath = "src/main/resources/file/SubjectPlan.xlsx";
    //             String moduleSemFilePath = "src/main/resources/file/ModuleSem.xlsx";
    //             String studentSemFilePath = "src/main/resources/file/StudentSem.xlsx";

    //             PreprocessingResult assignmentData = preprocessingService.preprocessModuleAndStudents(subjectPlanfilePath, 
    //                                                                                                         moduleSemFilePath, 
    //                                                                                                         studentSemFilePath);
    //             List<ModuleAssignmentData> assignmentDataList = assignmentData.getModuleAssignmentDataList();
    //             Map<Long, String> studentProgrammeMap = assignmentData.getStudentProgrammeMap();
    //             Map<Long, Integer> studentSemesterMap = assignmentData.getStudentSemesterMap();

    //             // Manually create the processor
    //             ModuleAssignmentProcessor processor = new ModuleAssignmentProcessor(lecturerService, 
    //                                                                                constraintEngine,
    //                                                                                clustering);

    //             // Run the assignment
    //             processor.processAssignments(assignmentDataList);
    //             processor.clusterProgrammeDistribution(assignmentDataList, studentProgrammeMap, studentSemesterMap);

    //             // Output results for verification
    //             // for(int i = 0; i < 300; i++){
    //             //     Session session = sessions.get(i);
    //             //     System.out.println(session);
    //             // }
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

    @Bean
    public CommandLineRunner akkaRun(VenueAssignApp venueAssignApp) {
        return args -> {
            venueAssignApp.runBlocking(args); // Change to blocking method
            logger.info(">>> Lecturer Service Runner Initialized <<<");
        };
    }
}
