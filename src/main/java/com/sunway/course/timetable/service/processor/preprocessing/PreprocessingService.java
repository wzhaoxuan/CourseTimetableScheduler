package com.sunway.course.timetable.service.processor.preprocessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sunway.course.timetable.helper.ModuleExcelHelper;
import com.sunway.course.timetable.model.Module;
import com.sunway.course.timetable.model.Student;
import com.sunway.course.timetable.model.SubjectPlanInfo;
import com.sunway.course.timetable.model.assignment.ModuleAssignmentData;
import com.sunway.course.timetable.model.assignment.ModuleSem;
import com.sunway.course.timetable.model.assignment.PreprocessingResult;
import com.sunway.course.timetable.model.assignment.StudentSem;
import com.sunway.course.timetable.model.programme.Programme;
import com.sunway.course.timetable.repository.ModuleRepository;
import com.sunway.course.timetable.repository.ProgrammeRepository;
import com.sunway.course.timetable.service.excelReader.SubjectPlanExcelReaderService;
import com.sunway.course.timetable.service.excelReader.ModuleSemExcelReaderService;
import com.sunway.course.timetable.service.excelReader.ProgrammeExcelReaderService;
import com.sunway.course.timetable.service.excelReader.StudentSemExcelReaderService;



/*
 * Read from the SubjectPlan excel file
 * Get the offered modules and enrolled students from programme
 * Add the data to ModuleAssignmentData
 */

@Service
public class PreprocessingService {

    private static Logger logger = LoggerFactory.getLogger(ProgrammeExcelReaderService.class);

    private final SubjectPlanExcelReaderService moduleExcelReaderService;
    private final ModuleRepository moduleRepository;
    private final ProgrammeRepository programmeRepository;
    private final ModuleSemExcelReaderService moduleSemExcelReaderService;
    private final StudentSemExcelReaderService studentSemExcelReaderService;


    public PreprocessingService(SubjectPlanExcelReaderService moduleExcelReaderService,
                                ModuleSemExcelReaderService moduleSemExcelReaderService,
                                StudentSemExcelReaderService studentSemExcelReaderService,
                                ModuleRepository moduleRepository,
                                ProgrammeRepository programmeRepository) {
        this.moduleExcelReaderService = moduleExcelReaderService;
        this.moduleSemExcelReaderService = moduleSemExcelReaderService;
        this.studentSemExcelReaderService = studentSemExcelReaderService;
        this.moduleRepository = moduleRepository;
        this.programmeRepository = programmeRepository;
    }

    public PreprocessingResult preprocessModuleAndStudents(String subjectPlanFilePath,
                                                            String moduleSemFilePath,
                                                            String studentSemFilePath) {
        List<ModuleAssignmentData> assignmentDataList = new ArrayList<>();
        Map<Long, String> studentProgrammeMap = new HashMap<>();
        Map<Long, Integer> studentSemesterMap = new HashMap<>();

        try {
                List<SubjectPlanInfo> subjectPlans = moduleExcelReaderService.readExcelFile(subjectPlanFilePath);
                Map<Integer, List<ModuleSem>> moduleSemMap = moduleSemExcelReaderService.readModuleSemExcelFile(moduleSemFilePath);
                Map<Integer, List<StudentSem>> studentSemMap = studentSemExcelReaderService.readStudentSemExcelFile(studentSemFilePath);

                for (Map.Entry<Integer, List<ModuleSem>> semEntry : moduleSemMap.entrySet()) {
                int semester = semEntry.getKey();
                List<ModuleSem> modulesInSem = semEntry.getValue();

                Set<String> moduleCodesInSemester = modulesInSem.stream()
                    .map(ModuleSem::getModuleId)
                    .collect(Collectors.toSet());

                List<StudentSem> studentsInSemester = studentSemMap.getOrDefault(semester, List.of());

                for (SubjectPlanInfo subject : subjectPlans) {
                    List<String> subjectCodes = ModuleExcelHelper.splitSubjectCode(subject.getSubjectCode());

                    for (String moduleCode : subjectCodes) {
                        if (!moduleCodesInSemester.contains(moduleCode)) {
                            continue; // skip modules not offered in this semester
                        }

                        Optional<Module> moduleOptional = moduleRepository.findById(moduleCode);
                        if (moduleOptional.isEmpty()) {
                            continue;
                        }

                        Module module = moduleOptional.get();

                        // Programmes offering the module
                        List<Programme> programmesOfferingModule = programmeRepository.findByModuleId(moduleCode);
                        Set<String> programmeCodes = programmesOfferingModule.stream()
                            .map(p -> p.getProgrammeId().getId())
                            .collect(Collectors.toSet());

                        Set<Student> eligibleStudents = new HashSet<>();
                        for (StudentSem studentSem : studentsInSemester) {
                            if (programmeCodes.contains(studentSem.getProgramme())) {
                                for (Programme programme : programmesOfferingModule) {
                                    Student student = programme.getStudent();
                                    if (student.getId() == studentSem.getStudentId()) {
                                        eligibleStudents.add(student);
                                        studentProgrammeMap.put(student.getId(), programme.getProgrammeId().getId());
                                        studentSemesterMap.put(student.getId(), semester);
                                        break;
                                    }
                                }
                            }
                        }

                        ModuleAssignmentData assignmentData = new ModuleAssignmentData(
                            subject,
                            module,
                            programmesOfferingModule,
                            eligibleStudents
                        );

                        assignmentDataList.add(assignmentData);
                    }
                }
            

                // Logging the semester grouping after processing each semester
            //     logger.info("=== Semester-wise Module Assignment Data ===");
            //     logger.info("Semester {}", semester);
            //     for (ModuleSem moduleSem : modulesInSem) {
            //         String moduleId = moduleSem.getModuleId();

            //         List<ModuleAssignmentData> dataForModule = assignmentDataList.stream()
            //             .filter(d -> d.getModule().getId().equals(moduleId))
            //             .collect(Collectors.toList());

            //         for (ModuleAssignmentData data : dataForModule) {
            //             logger.info("  Module: {}", moduleId);
            //             String studentsList = data.getEligibleStudents().stream()
            //                 .map(student -> String.valueOf(student.getId()))
            //                 .sorted()
            //                 .collect(Collectors.joining(", "));
            //             logger.info("    Students: {}", studentsList);
            //         }
            //     }
            //     logger.info("-----------------------------------------------------------------------------------------------------------");
            }

        } catch (Exception e) {
            logger.error("Error reading Excel file: {}", e.getMessage());
            e.printStackTrace();
        }

        return new PreprocessingResult(assignmentDataList, studentProgrammeMap, studentSemesterMap);
    }


    private int findSemesterForModule(Map<Integer, List<ModuleSem>> moduleSemMap, String moduleCode) {
        for (Map.Entry<Integer, List<ModuleSem>> entry : moduleSemMap.entrySet()) {
            for (ModuleSem moduleSem : entry.getValue()) {
                if (moduleSem.getModuleId().equals(moduleCode)) {
                    return entry.getKey();
                }
            }
        }
        return -1; // Not found
    }
}
