package com.sunway.course.timetable.service.excelReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sunway.course.timetable.helper.ProgrammeExcelHelper;
import com.sunway.course.timetable.model.Module;
import com.sunway.course.timetable.model.Student;
import com.sunway.course.timetable.model.programme.Programme;
import com.sunway.course.timetable.model.programme.ProgrammeId;
import com.sunway.course.timetable.repository.ModuleRepository;
import com.sunway.course.timetable.repository.ProgrammeRepository;
import com.sunway.course.timetable.repository.StudentRepository;
import com.sunway.course.timetable.util.DateUtil;
import com.sunway.course.timetable.util.ExcelUtil;

import jakarta.transaction.Transactional;

/*
 * This service reads programme data from an Excel file and processes it to create Programme entities.
 */

@Service
public class ProgrammeExcelReaderService {

    private static Logger logger = LoggerFactory.getLogger(ProgrammeExcelReaderService.class);

    private final StudentRepository studentRepository;
    private final ModuleRepository moduleRepository;
    private final ProgrammeRepository programmeRepository;

    public ProgrammeExcelReaderService(StudentRepository studentRepository, 
                                        ModuleRepository moduleRepository, 
                                        ProgrammeRepository programmeRepository) {
        this.studentRepository = studentRepository;
        this.moduleRepository = moduleRepository;
        this.programmeRepository = programmeRepository;
    }

    @Transactional
    public void processProgrammeExcelData(String filePath) throws FileNotFoundException {
        try(InputStream inputStream = new FileInputStream(filePath); 
            Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Map<String, List<Student>> programmeToStudents = ProgrammeExcelHelper.assignStudentsToProgrammesEqually(studentRepository.findAll());
            Map<Long, Programme> studentProgrammeMetadata = new HashMap<>();
            Map<String, Integer> headerMap = ExcelUtil.getHeaderMap(sheet.getRow(0));
            
            // Loop through the rows and process data
            for(int i = 1; i <= sheet.getLastRowNum(); i++){
                Row row = sheet.getRow(i);
                if(row == null) continue; // Skip empty rows

                processRow(row, headerMap, programmeToStudents, studentProgrammeMetadata);
            }
        } catch (IOException e) {
            logger.error("Failed to process Excel file: {}", e.getMessage(), e);
        }
    }   


    private void processRow(Row row, Map<String, Integer> headerMap,
                            Map<String, List<Student>> programmeToStudents,
                            Map<Long, Programme> studentProgrammeMetadata) {
        String programmeId = ExcelUtil.getCellValue(row, headerMap.get("Programme ID"));
        String moduleId = ExcelUtil.getCellValue(row, headerMap.get("Module ID"));
        String startYearStr = ExcelUtil.getCellValue(row, headerMap.get("Start Year"));
        String endYearStr = ExcelUtil.getCellValue(row, headerMap.getOrDefault("End Year", -1));

        if(programmeId.isEmpty() || moduleId.isEmpty() || startYearStr.isEmpty()) return;

        int startYear = Integer.parseInt(startYearStr);
        int endYear = endYearStr.isEmpty() ? DateUtil.getCurrentYear() : Integer.parseInt(endYearStr);

        // Check if the module ID is valid
        Module module = moduleRepository.findById(moduleId).orElse(null);
        if(module == null){
            logger.warn("Module not found: {}", moduleId);
            return; // Skip if module not found
        }

        List<Student> students = programmeToStudents.getOrDefault(programmeId, new ArrayList<>());
        for(Student student : students){
            createEligibleProgramme(programmeId, module, student, startYear, endYear, studentProgrammeMetadata);
        }
    }

    private void createEligibleProgramme(String programmeId, Module module, Student student, 
                                        int startYear, int endYear,
                                        Map<Long, Programme> studentProgrammeMetadata) {
        Long studentId = student.getId();
        // Extract year from student ID
        int enrollmentYear = 2000 + Integer.parseInt(studentId.toString().substring(0, 2));

        if(enrollmentYear >= startYear && enrollmentYear <= endYear){
            Programme programmeMeta = studentProgrammeMetadata.computeIfAbsent(studentId, 
            sid -> buildProgrammeMetaData(programmeId, student, enrollmentYear));

            ProgrammeId id = new ProgrammeId(programmeId, studentId, module.getId());
            Programme programme = new Programme();
            programme.setProgrammeId(id);
            programme.setStudent(student);
            programme.setModule(module);
            programme.setName(programmeMeta.getName());
            programme.setYear(programmeMeta.getYear());
            programme.setIntake(programmeMeta.getIntake());
            programme.setDuration(programmeMeta.getDuration());
            programme.setSemester(programmeMeta.getSemester());

            programmeRepository.save(programme);
            // logger.info("Programme saved: {} for student: {}", programme.getName(), student.getId());
        }
    }

    private Programme buildProgrammeMetaData(String programmeId, Student student, int enrollmentYear) {
        Programme programme = new Programme();
        programme.setStudent(student);
        programme.setName(ProgrammeExcelHelper.assignProgrammeName(programmeId));
        programme.setYear(enrollmentYear);
        programme.setIntake(ProgrammeExcelHelper.getRandomIntake());
        programme.setDuration(3);
        programme.setSemester(9);
        return programme;
    }
}
