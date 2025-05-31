// package com.sunway.course.timetable.service;
// import java.util.List;
// import java.util.Map;
// import java.util.stream.Collectors;

// import org.springframework.stereotype.Service;

// import com.sunway.course.timetable.model.Session;
// import com.sunway.course.timetable.model.assignment.ModuleAssignmentData;
// import com.sunway.course.timetable.model.assignment.PreprocessingResult;
// import com.sunway.course.timetable.service.processor.ModuleAssignmentProcessor;
// import com.sunway.course.timetable.service.processor.preprocessing.PreprocessingService;

// @Service
// public class ModuleAssignmentService {

//     private final ModuleAssignmentProcessor moduleAssignmentProcessor;
//     private final PreprocessingService preprocessingService;

//     public ModuleAssignmentService(ModuleAssignmentProcessor processor,
//                                    PreprocessingService preprocessingService) {
//         this.moduleAssignmentProcessor = processor;
//         this.preprocessingService = preprocessingService;
//     }

//     public Map<Session, ModuleAssignmentData> getSessionToModuleAssignmentMap(
//         String subjectPlanFilePath,
//         String moduleSemFilePath,
//         String studentSemFilePath) {

//         List<ModuleAssignmentData> dataList = preprocessingService
//                 .preprocessModuleAndStudents(subjectPlanFilePath, moduleSemFilePath, studentSemFilePath)
//                 .getModuleAssignmentDataList();

//         return moduleAssignmentProcessor.mapSessionsToAssignments(dataList);
//     }

//     public List<ModuleAssignmentData> getFilteredModuleAssignments(
//             String subjectPlanFilePath,
//             String moduleSemFilePath,
//             String studentSemFilePath,
//             String programme,
//             String year,
//             String intake,
//             String semester) {

//         List<ModuleAssignmentData> allDataList = preprocessingService
//                 .preprocessModuleAndStudents(subjectPlanFilePath, moduleSemFilePath, studentSemFilePath)
//                 .getModuleAssignmentDataList();

//         return allDataList.stream()
//                 .filter(data -> data.getProgrammeOfferingModules().stream()
//                         .anyMatch(p -> p.getProgrammeId().getId().equals(programme)
//                                 // You can add more filtering here for year, intake, semester if your data supports it
//                                 // e.g. && p.getYear() == Integer.parseInt(year)
//                                 // && p.getIntake().equals(intake)
//                                 // && p.getSemester() == Integer.parseInt(semester)
//                         )
//                 )
//                 .collect(Collectors.toList());
//     }

//     public PreprocessingResult getFullPreprocessingResult(
//         String subjectPlanFilePath,
//         String moduleSemFilePath,
//         String studentSemFilePath) {

//         return preprocessingService.preprocessModuleAndStudents(subjectPlanFilePath, moduleSemFilePath, studentSemFilePath);
//     }

// }
