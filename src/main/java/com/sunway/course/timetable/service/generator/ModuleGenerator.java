package com.sunway.course.timetable.service.generator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sunway.course.timetable.config.SubjectCreditHourConfig;
import com.sunway.course.timetable.helper.ModuleExcelHelper;
import com.sunway.course.timetable.model.Module;
import com.sunway.course.timetable.repository.ModuleRepository;

@Service
public class ModuleGenerator {

    private static Logger logger = LoggerFactory.getLogger(VenueDistanceGenerator.class);

    private final ModuleRepository moduleRepository;

    public ModuleGenerator(ModuleRepository moduleRepository){
        this.moduleRepository = moduleRepository;
    }

    public void saveModulesFromExcel(String subjectCode, String subjectName){
        List<String> codes = ModuleExcelHelper.splitSubjectCode(subjectCode);
        
        if(subjectCode == null || subjectCode.isBlank()) return;
        for (String code : codes) {
            if (!moduleRepository.existsById(code)) {
                int creditHour = SubjectCreditHourConfig.getCreditHour(code);
                moduleRepository.save(new Module(code, subjectName, creditHour));
            }
        }
    }
}
