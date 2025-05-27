package com.sunway.course.timetable.service.processor.preprocessing;

import java.util.ArrayList;
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
import com.sunway.course.timetable.model.assignment.StudentSem;
import com.sunway.course.timetable.model.programme.Programme;
import com.sunway.course.timetable.repository.ModuleRepository;
import com.sunway.course.timetable.repository.ProgrammeRepository;
import com.sunway.course.timetable.service.excelReader.ModuleExcelReaderService;
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

    private final ModuleExcelReaderService moduleExcelReaderService;
    private final ModuleRepository moduleRepository;
    private final ProgrammeRepository programmeRepository;
    private final ModuleSemExcelReaderService moduleSemExcelReaderService;
    private final StudentSemExcelReaderService studentSemExcelReaderService;


    public PreprocessingService(ModuleExcelReaderService moduleExcelReaderService,
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

    public List<ModuleAssignmentData> preprocessModuleAndStudents(String subjectPlanFilePath,
                                                                 String moduleSemFilePath,
                                                                    String studentSemFilePath) {
        List<ModuleAssignmentData> assignmentDataList = new ArrayList<>();

        try {
            List<SubjectPlanInfo> subjectPlans = moduleExcelReaderService.readExcelFile(subjectPlanFilePath);
            Map<Integer, List<ModuleSem>> moduleSemMap = moduleSemExcelReaderService.readModuleSemExcelFile(moduleSemFilePath);
            Map<Integer, List<StudentSem>> studentSemMap = studentSemExcelReaderService.readStudentSemExcelFile(studentSemFilePath);

            for(SubjectPlanInfo subject: subjectPlans){
                // Split the subject code into individual module codes
                List<String> subjectCode = ModuleExcelHelper.splitSubjectCode(subject.getSubjectCode());

                // For each module code, find the corresponding Module entity
                for(String moduleCode: subjectCode){
                    Optional<Module> moduleOptional = moduleRepository.findById(moduleCode);
                    if(moduleOptional.isEmpty()){
                        logger.info("Module with code {} not found in database", subjectCode);
                        continue;
                    }

                    // If module is found, get its credit hour
                    Module module = moduleOptional.get();

                    // Find semester that offers this module
                    int semester = findSemesterForModule(moduleSemMap, moduleCode);
                    if (semester == -1) {
                        logger.warn("No semester found for module: {}", moduleCode);
                        continue;
                    }

                    // Get all programmes associated with the module
                    List<Programme> programmesOfferingModule  = programmeRepository.findByModuleId(moduleCode);
                    Set<String> programmeCodes = programmesOfferingModule.stream()
                            .map(p -> p.getProgrammeId().getId())
                            .collect(Collectors.toSet());

                    // Filter students by:
                    // - Belonging to one of these programmes
                    // - Matching the module's offered semester
                    Set<Student> eligibleStudents = new HashSet<>();
                    List<StudentSem> studentsInSemester = studentSemMap.getOrDefault(semester, List.of());

                    for (StudentSem studentSem : studentsInSemester) {
                        if (programmeCodes.contains(studentSem.getProgramme())) {
                            for (Programme programme : programmesOfferingModule) {
                                Student student = programme.getStudent();
                                if (student.getId() == studentSem.getStudentId()) {
                                    eligibleStudents.add(student);
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
            
        } catch (Exception e) {
            logger.error("Error reading Excel file: {}", e.getMessage());
            e.printStackTrace();
        }

        // logger.info("=== Module Assignment Data Details ===");
        // for (ModuleAssignmentData data : assignmentDataList) {
        //     logger.info("SubjectPlanInfo: {}", data.getSubjectPlanInfo());
        //     logger.info("Module: {}", data.getModule().getId());
            
        //     String programmeCodes = data.getProgrammeOfferingModules().stream()
        //         .map(p -> p.getProgrammeId().getId())
        //         .collect(Collectors.joining(", "));
        //     logger.info("Programmes: {}", programmeCodes);

        //     String studentIds = data.getEligibleStudents().stream()
        //         .map(s -> s.getId().toString())
        //         .collect(Collectors.joining(", "));
        //     logger.info("Eligible Students: {}", studentIds);

        //     logger.info("--------------------------------------------------");
        // }
        return assignmentDataList;
        
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
