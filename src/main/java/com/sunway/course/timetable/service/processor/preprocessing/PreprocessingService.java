package com.sunway.course.timetable.service.processor.preprocessing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sunway.course.timetable.helper.ModuleExcelHelper;
import com.sunway.course.timetable.model.Module;
import com.sunway.course.timetable.model.Student;
import com.sunway.course.timetable.model.SubjectPlanInfo;
import com.sunway.course.timetable.model.assignment.ModuleAssignmentData;
import com.sunway.course.timetable.model.programme.Programme;
import com.sunway.course.timetable.repository.ModuleRepository;
import com.sunway.course.timetable.repository.ProgrammeRepository;
import com.sunway.course.timetable.service.excelReader.ModuleExcelReaderService;
import com.sunway.course.timetable.service.excelReader.ProgrammeExcelReaderService;

@Service
public class PreprocessingService {

    private static Logger logger = LoggerFactory.getLogger(ProgrammeExcelReaderService.class);

    private final ModuleExcelReaderService moduleExcelReaderService;
    private final ModuleRepository moduleRepository;
    private final ProgrammeRepository programmeRepository;

    public PreprocessingService(ModuleExcelReaderService moduleExcelReaderService,
                                ModuleRepository moduleRepository,
                                ProgrammeRepository programmeRepository) {
        this.moduleExcelReaderService = moduleExcelReaderService;
        this.moduleRepository = moduleRepository;
        this.programmeRepository = programmeRepository;
    }

    public List<ModuleAssignmentData> preprocessModuleAndStudents(String filePath){
        List<ModuleAssignmentData> assignmentDataList = new ArrayList<>();

        try {
            List<SubjectPlanInfo> subjectPlans = moduleExcelReaderService.readExcelFile(filePath);

            for(SubjectPlanInfo subject: subjectPlans){
                List<String> subjectCode = ModuleExcelHelper.splitSubjectCode(subject.getSubjectCode());

                for(String code: subjectCode){
                    Optional<Module> moduleOptional = moduleRepository.findById(code);
                    if(moduleOptional.isEmpty()){
                        logger.info("Module with code {} not found in database", subjectCode);
                        continue;
                    }

                    Module module = moduleOptional.get();
                    int creditHour = module.getCreditHour();

                    // Get all programmes associated with the module
                    List<Programme> programmes = programmeRepository.findByModuleId(code);

                    // Collect students from all programmes
                    Set<Student> eligibleStudents = new HashSet<>();
                    for(Programme programme: programmes){
                        eligibleStudents.add(programme.getStudent());
                    }

                    ModuleAssignmentData assignmentData = new ModuleAssignmentData(subject, module, programmes, eligibleStudents);
                    assignmentDataList.add(assignmentData);

                    logger.info("Subject Code: {} | Credit Hour: {} | Programmes: {} | Eligible Students: {}", 
                                code, creditHour, programmes.size(), eligibleStudents.size(), eligibleStudents);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error reading Excel file: {}", e.getMessage());
            e.printStackTrace();
        }

        return assignmentDataList;
        
    }

}
