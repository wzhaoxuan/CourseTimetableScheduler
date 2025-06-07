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

import com.sunway.course.timetable.helper.ModuleExcelHelper;
import com.sunway.course.timetable.model.assignment.StudentSem;
import com.sunway.course.timetable.util.ExcelUtil;

@Service
public class StudentSemExcelReaderService {

    private static Logger logger = LoggerFactory.getLogger(StudentSemExcelReaderService.class);

    public Map<Integer, List<StudentSem>> readStudentSemExcelFile(String filePath) throws FileNotFoundException {
        Map<Integer, List<StudentSem>> studentSemMap = new HashMap<>();
        try (InputStream inputStream = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Map<String, Integer> headerMap = ExcelUtil.getHeaderMap(sheet.getRow(0));

            for(int i = 1; i <= sheet.getLastRowNum(); i++){
                Row row = sheet.getRow(i);
                if(row == null) continue; // Skip empty rows

                int semseter =  ModuleExcelHelper.parseInt(ExcelUtil.getCellValue(row, headerMap.get("Semester")));
                int studentId = ModuleExcelHelper.parseInt(ExcelUtil.getCellValue(row, headerMap.get("StudentID")));
                String programme = ExcelUtil.getCellValue(row, headerMap.get("Programme"));

                StudentSem studentSem = new StudentSem(studentId, programme, semseter);

                // Add the StudentSem object to the map under the corresponding semester
                studentSemMap.computeIfAbsent(semseter, k -> new ArrayList<>()).add(studentSem);
            }
        } catch (IOException e) {
            logger.error("Error reading Student Semester Excel file: {}", e.getMessage());
        }

        // logger.info("Student Sem: {}", studentSemMap);
        return studentSemMap;
    }

}
