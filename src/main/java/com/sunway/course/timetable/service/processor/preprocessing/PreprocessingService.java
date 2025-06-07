package com.sunway.course.timetable.service.processor.preprocessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import com.sunway.course.timetable.service.excelReader.ModuleSemExcelReaderService;
import com.sunway.course.timetable.service.excelReader.StudentSemExcelReaderService;
import com.sunway.course.timetable.service.excelReader.SubjectPlanExcelReaderService;



/*
 * Read from the SubjectPlan excel file
 * Get the offered modules and enrolled students from programme
 * Add the data to ModuleAssignmentData
 */

@Service
public class PreprocessingService {

    private static Logger logger = LoggerFactory.getLogger(PreprocessingService.class);

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
        Map<String, Set<Integer>> moduleIdToSemestersMap = new HashMap<>();
        Map<Integer, List<StudentSem>> studentSemMap = new HashMap<>();

        try {
                List<SubjectPlanInfo> subjectPlans = moduleExcelReaderService.readExcelFile(subjectPlanFilePath);
                Map<Integer, List<ModuleSem>> moduleSemMap = moduleSemExcelReaderService.readModuleSemExcelFile(moduleSemFilePath);
                studentSemMap = studentSemExcelReaderService.readStudentSemExcelFile(studentSemFilePath);

                 // === Step 1: Build moduleId → semester set
            for (Map.Entry<Integer, List<ModuleSem>> entry : moduleSemMap.entrySet()) {
                int semester = entry.getKey();
                for (ModuleSem ms : entry.getValue()) {
                    moduleIdToSemestersMap.computeIfAbsent(ms.getModuleId(), k -> new HashSet<>()).add(semester);
                }
            }

            // === Step 2: Combine modules by ID and build assignment data
            for (SubjectPlanInfo subject : subjectPlans) {
                List<String> moduleCodes = ModuleExcelHelper.splitSubjectCode(subject.getSubjectCode());

                for (String moduleCode : moduleCodes) {
                    Optional<Module> moduleOpt = moduleRepository.findById(moduleCode);
                    if (moduleOpt.isEmpty()) continue;

                    Module module = moduleOpt.get();
                    // logger.info("Processing module: {}", module.getId());
                    List<Programme> programmesOfferingModule = programmeRepository.findByModuleId(moduleCode);
                    Set<String> programmeCodes = programmesOfferingModule.stream()
                            .map(p -> p.getProgrammeId().getId())
                            .collect(Collectors.toSet());

                    Set<Student> combinedEligibleStudents = new HashSet<>();
                    Set<Integer> semesters = moduleIdToSemestersMap.getOrDefault(moduleCode, Set.of());

                    for (int sem : semesters) {
                        List<StudentSem> studentsInSem = studentSemMap.getOrDefault(sem, List.of());

                        for (StudentSem s : studentsInSem) {
                            if (!programmeCodes.contains(s.getProgramme())) continue;

                            for (Programme programme : programmesOfferingModule) {
                                Student student = programme.getStudent();
                                if (student.getId() == s.getStudentId()) {
                                    combinedEligibleStudents.add(student);
                                    studentProgrammeMap.put(student.getId(), programme.getProgrammeId().getId());
                                    studentSemesterMap.put(student.getId(), sem); // Last sem wins (OK for merged)
                                    break;
                                }
                            }
                        }
                    }

                    if (!combinedEligibleStudents.isEmpty()) {
                        ModuleAssignmentData data = new ModuleAssignmentData(
                            subject,
                            module,
                            programmesOfferingModule,
                            combinedEligibleStudents
                        );
                        assignmentDataList.add(data);
                    }
                }
            }

             // Logging the semester grouping after processing each semester
        //     logger.info("=== Combined Module Assignment Summary ===");
        //     for (ModuleAssignmentData data : assignmentDataList) {
        //         String moduleId = data.getModule().getId();
        //         String subjectTitle = data.getSubjectPlanInfo().getSubjectCode();
        //         Set<Long> studentIds = data.getEligibleStudents().stream()
        //             .map(Student::getId)
        //             .collect(Collectors.toSet());

        //         logger.info("Module ID: {}, Title: {}", moduleId, subjectTitle);
        //         logger.info("  Total Students: {}", studentIds.size());
        //         logger.info("  Student IDs: {}", studentIds.stream().sorted().map(String::valueOf).collect(Collectors.joining(", ")));
        //     }
        //     logger.info("===================================================");

        } catch (Exception e) {
            logger.error("Error reading Excel file: {}", e.getMessage());
            e.printStackTrace();
        }

        return new PreprocessingResult(assignmentDataList, studentProgrammeMap, studentSemesterMap);
    }
}
